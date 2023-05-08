doc = """
This is intended to be a temporal accessory tool to convert FNS simulated data
into gephi compliant csv files. We are aware of the limitations of
this approach. In the close future we want to implement an option
in FNS simulator (i.e. a switcher) to obtain these csv gephi compliant
files in the most efficient way.

Until then: well, this will be useful.

Let's go.
"""

import os
from collections import Counter
from datetime import datetime

import pandas as pd
import numpy as np


def init(mode):
    """
    First, we need to know where FNS (matlab compliant version) data is,
    and some information about the simulation: simulation length in
    milliseconds and number of nodes.

    :param mode: "e" expert, parameters input in one line; else asking each parameter.
    :return: parameters: data_path, simulation time, nodes.
    """

    # Gather information in one line
    if mode == "e":
        data_path, simtime, nodes = \
            input("comma separated: path, simtime, nodes").split(", ")
        simtime = int(simtime)
        nodes = int(nodes)

    # Ask for information
    else:
        data_path = input(" Path to FNS data (matlab compliant): ")
        simtime = int(input(" Simulation time (ms): "))
        nodes = int(input(" Number of nodes: "))

    return data_path, simtime, nodes


def conversion(data_path, nodes, simtime, resolution=1):
    """
     This function executes two loops, the first one to gather information
     about edges in the network, the second one to gather neurons' activity
     in a table called "status_table".

     Then information will be structured to be exported to gephi, including info
     about nodes, edges and activity.

     :param data_path: path to FNS simulated data (must be the MATLAB compliant
     version).
     :param nodes: number of nodes in the simulation by FNS definition of
     node (i.e. region).
     :param simtime: Simulation length in milliseconds
     :param resolution: number of time windows per millisecond in which events
     will be added.
     :return:
     """

    receivers = set()  # i.e. neurons; senders can be external inputs
    edges_set = set()

    # Ask number of neurons per node
    nodes_dict = dict()
    if input("Every node has same number of neurons? (y/n)") == "y":
        n = int(input("Number of neurons per node: "))
        for node in range(nodes):
            nodes_dict[node] = n
            receivers.update([str(node) + "-" + str(neuron) for neuron in range(nodes_dict[node])])
    else:
        for node in range(nodes):
            nodes_dict[node] = int(input("Number of neurons in node %s: " % node))
            receivers.update([str(node) + "-" + str(neuron) for neuron in range(nodes_dict[node])])

    receivers = sorted(list(receivers))

    # Load a part of FNS data to gather information about edges
    df = pd.read_csv(data_path, header=None, usecols=[0, 1, 2, 3, 4, 5], chunksize=500000, nrows=2000000)
    for chunk in df:
        chunk.columns = ["Burning Time", "Firing Node", "Firing Neuron", "Burning Node", "Burning Neuron",
                         "External Source"]
        print(chunk.index)
        for node in range(nodes):
            temp = chunk.loc[
                (chunk["Burning Node"] == node) & (chunk["External Source"] == False)]  # Remove external inputs
            for i in range(len(temp)):
                print("Gathering edge information for node: %i/%i. Connection: %i/%i" % (
                    node + 1, nodes, i + 1, len(temp)), end="\r")
                sender = str(np.asarray(temp["Firing Node"])[i]) + "-" + str(np.asarray(temp["Firing Neuron"])[i])
                receiver = str(node) + "-" + str(np.asarray(temp["Burning Neuron"])[i])
                edges_set.add((sender, receiver))
            print("Gathering edge information for node: %i/%i. Connection: %i/%i" % (node + 1, nodes, i + 1, len(temp)))

    status_table = pd.DataFrame(np.zeros((len(receivers), len(range((simtime - 1) * resolution)))),
                                columns=list(range((simtime - 1) * resolution)), index=receivers)

    df = pd.read_csv(data_path, header=None, usecols=[0, 1, 2, 3, 4, 5], chunksize=1000000)
    for chunk in df:
        chunk.columns = ["Burning Time", "Firing Node", "Firing Neuron", "Burning Node", "Burning Neuron",
                         "External Source"]
        print(chunk.index)

        # Loop over times instead of receivers: slightly better efficiency for large datasets
        times_raw = sorted(set(chunk["Burning Time"]))
        status_table_temp = pd.DataFrame(columns=receivers)
        status_table_aux = pd.DataFrame(np.zeros((len(receivers), len(range((simtime - 1) * resolution)))),
                                        columns=list(range((simtime - 1) * resolution)), index=receivers)

        times = list(np.arange(0, int(max(times_raw) + 1), 1 / resolution))
        min_t = int(np.trunc(min(chunk["Burning Time"])))
        max_t = int(np.trunc(max(chunk["Burning Time"])))

        for t in times[min_t:max_t]:
            print("Gathering nodes' activity dynamics - t: %i/%i" % (t + 1, max_t), end="\r")
            temp = chunk.loc[np.trunc(chunk["Burning Time"]) == t, ("Burning Node", "Burning Neuron")]
            # Count burning events for each neuron at time t
            count = Counter([str(node) + "-" + str(neuron) for node, neuron in np.asarray(temp)])
            # Every time step we add a Counter dict to fill status table
            status_table_temp = status_table_temp.append(count, ignore_index=True)
        print("Gathering nodes' activity dynamics - t: %i/%i" % (t + 1, max_t))

        # status_table_temp contains just a limited space of time each chunk
        status_table_temp = status_table_temp.transpose()
        status_table_temp.columns = list(
            np.arange(start=int(np.trunc(min(chunk["Burning Time"]))), stop=int(np.trunc(max(chunk["Burning Time"])))))
        status_table_temp = status_table_temp.fillna(0)

        # status_table_aux contains all time steps with temp table values and 0s for the rest each chunk
        status_table_aux = status_table_aux.add(status_table_temp)
        status_table_aux = status_table_aux.fillna(0)

        # status_table merges all chunks' data
        status_table = status_table.add(status_table_aux)
        status_table = status_table.fillna(0)

    timestamp = datetime.now()
    new_dir = "gephiFiles" + timestamp.strftime("d%d_%m_%Y-t%H_%M_%S")
    os.mkdir(new_dir)

    # Generate gephi compliant files
    t = "<" + str(times) + ">"
    gephi_nodes = pd.DataFrame(columns=["id", "label", "timeset", "events", "node"])
    for idx, node in enumerate(receivers):
        print("Writing nodes' file for Gephi: %i/%i" % (idx, len(receivers)), end="\r")
        events_row = [[float(i), int(events)] for i, events in enumerate(status_table.loc[node])]
        events_row = str(events_row).replace("[[", "<[").replace("]]", "]>").replace("],", "];")
        new_row = pd.Series([node, node, t, events_row, node.split("-")[0]], index=gephi_nodes.columns)
        gephi_nodes = gephi_nodes.append(new_row, ignore_index=True)
    print("Writing nodes' file for Gephi: %i/%i" % (idx, len(receivers)))
    gephi_nodes.to_csv(new_dir + "/gephi_nodes.csv", index=False)

    print("Compute gephi files with %i edges will last %0.2fm approx." % (len(edges_set), len(edges_set) / 12000))
    if input("Do you want to proceed? (y/n) ") == "n":
        exit()
    gephi_edges = pd.DataFrame(columns=["Source", "Target", "type", "id", "weight"])
    for idx, edge in enumerate(edges_set):
        print("Writing edges' file for Gephi: %i/%i" % (idx, len(edges_set)), end="\r")
        edge_row = pd.Series([edge[0], edge[1], "Directed", idx, 1], index=gephi_edges.columns)
        gephi_edges = gephi_edges.append(edge_row, ignore_index=True)
    print("Writing edges' file for Gephi: %i/%i" % (idx, len(edges_set)))
    gephi_edges.to_csv(new_dir + "/gephi_edges.csv", index=False)

    return None

mode = input("""
Welcome to FNS-gephi converter.

First time? -> type "more"; Expert? -> "e". 
Otherwise press enter.
""")

if mode == "more":
    print(doc)
    print(init.__doc__)
    input("If you need more help contact us. Press enter to continue.")

data_path, nodes, simtime = init(mode)

conversion(data_path, nodes, simtime)
