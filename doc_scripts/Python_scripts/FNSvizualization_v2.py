"""
This tool will allow you to visualize FNS neuronal simulated activity.
The user must bear in mind that large simulations could require long time to
be computed. As far as we can, we will show a time estimation before runs.

Simulation visualization can be arranged in two ways:

    - neuronal activity visualization. An algorithm to generate a movie
     where each ms of network activity is used as a frame. Here we plot
     every neuron, with the possibility of adding edges. We dont recommend
     plotting edges for simulations with more than 2000 edges.

    - nodal (regional) activity visualization. Same as before, but this time we focus
     on the average activity of nodes (groups of neurons). This framework
     is more efficient but it losses accuracy.

It must be noted the procedure by which we transform FNS datasets. We loop
 over the events recorded by FNS collecting nodes and neurons' names and
 connections between them. Be aware that short simulations could give the
 result that some connections were not activated and they are not identified
 by our algorithm - so they are not included in our visualizations -.
 On the other hand, for long simulations, our algorithm revise a part
 of the original dataset: 2.000.000 events in the  neuronal mode.
 This must be enough to capture neurons and connections in the model.

This is meant to be a decision pro code's efficiency. Please report any
 bugs detected in the system.
"""

import os
from collections import Counter
from datetime import datetime
import random

import igraph
import cv2
import glob
import pandas as pd
import numpy as np
import plotly.express as px
import plotly.graph_objects as go
import plotly.io as pio


def init(mode):
    """
        This visualization toolkit will allow you to plot neural activity in time over
        simulation's network structure. We offer two levels of abstraction: neuronal
        level and regional level.

        First of all we will need to gather some information about the FNS
        simulation parameters.

        We will ask you for:

            - abstraction: sometimes is preferable to explore what is happening at neuronal
            scale and sometimes this is not as useful as having a representation of the average
            activity of nodes. The latter is lighter and easier to compute - it will take shorter time.
            Options: nodes, neurons

            - layout: igraph layout. We obtained interesting results with
            Kamada-Kaway and Fruchterman-reingold algorithms. Options: def="kk", "fr", among others.
            More info at https://igraph.org/c/doc/igraph-Layout.html

            - show edges: showing the edges could be computationally expensive in large simulations.
            By default the code estimates how long it will take to plot them and make a decision (less
            than 2 minutes they will be plotted). Options: "True", "False", def="auto"

            among other things...

        Let's go.
        """

    # Gather information in one line
    if mode == "e":
        data_path, abstraction, simtime, nodes, layout, show_edges, npn = \
            input("Comma separated: path, abstraction, simtime, nodes, layout, show_edges, neurons_per_node").split(", ")
        simtime = int(simtime)
        nodes = int(nodes)
        npn = int(npn)

    # Ask for information
    else:
        data_path = input(" Path to FNS data (matlab compliant): ")
        simtime = int(input(" Simulation time (ms): "))
        nodes = int(input(" Number of nodes: "))
        abstraction = input("Visualization abstraction (node OR neuron): ")
        default = input(" Keep in_python defaults? y/n: ")
        if default == "n":
            layout = input(" igraph network layout (kk, fr, ): ")
            show_edges = input(" show edges mode (True, False, auto): ")

    return data_path, abstraction, simtime, nodes, layout, show_edges, npn


def macroviz(data_path, nodes, simtime, npn=None, layout="kk", show_edges="auto", resolution=1):
        """
        This is a more efficient approach although it takes away part of the
        accuracy of the simulation.

        The algorithm creates a table "status_table" where it registers each node's activity (i.e.
        summing neuron burning events in a node) for every timepoint.

        It gathers network information in "nodes_dict" (number of neurons per node) and "edges_set", a set
        - i.e. a list of unique values - of edges between neurons. edges_set will be used to compute nodes' degree.

        Data is loaded in chunks because long simulations could cause the system go out of memory.

        :param data_path: path to FNS simulated data (must be the MATLAB compliant
        version).
        :param nodes: number of nodes in the simulation by FNS definition of
        node (i.e. region).
        :param simtime: Simulation length in milliseconds
        :param layout:  gephi graph layout; def="fr", "kk", ..
        :param show_edges:  True to plot edges. We dont
        recommend plotting large amount of edges. It slows down dramatically
        the computation.
        :param resolution: number of plots per millisecond
        :param npn: number of neurons per node passed in expert mode
        :return:
        """

        # Ask number of neurons per node
        nodes_dict = dict()
        if npn:
            for node in range(nodes):
                nodes_dict[node] = npn

        elif input("Every node has same number of neurons? (y/n)") == "y":
            n = int(input("Number of neurons per node: "))
            for node in range(nodes):
                nodes_dict[node] = n
        else:
            for node in range(nodes):
                nodes_dict[node] = int(input("Number of neurons in node %s: " % node))

        # Create status_table and edges_set to be filled
        status_table = pd.DataFrame(np.zeros((nodes, len(range(simtime * resolution)))),
                                    columns=list(range(simtime * resolution)))
        edges_set = set()

        # Load FNS simulated data in chunks
        df = pd.read_csv(data_path, header=None, usecols=[0, 1, 2, 3, 4], chunksize=1000000)
        print("Creating a complete status table and updating edge connectivity set. By chunks.")
        for chunk in df:
            chunk.columns = ["Burning Time", "Firing Node", "Firing Neuron", "Burning Node", "Burning Neuron"]
            print(chunk.index)  # So we can control loop's progress

            # We will merge this temporal dataframe with status_table each loop step
            status_table_temp = pd.DataFrame(columns=list(range(simtime * resolution)))

            # we focus on one burning node, and remove self-connections + external inputs
            for node in range(nodes):
                temp = chunk.loc[(chunk["Burning Node"] == node) & (chunk["Firing Node"] != node)]
                for i in range(len(temp)):
                    # Neuron naming will be: node-neuron (e.g. 0-14, node 0 - neuron 14)
                    sender = str(np.asarray(temp["Firing Node"])[i]) + "-" + str(np.asarray(temp["Firing Neuron"])[i])
                    receiver = str(node) + "-" + str(np.asarray(temp["Burning Neuron"])[i])
                    edges_set.add((sender, receiver))

                # Fill status table counting burning events
                temp = chunk.loc[chunk["Burning Node"] == node, "Burning Time"]
                count = Counter(np.trunc(np.asarray(temp)))
                status_table_temp = status_table_temp.append(count, ignore_index=True)
                status_table_temp = status_table_temp.fillna(0)
            # We use add because where one temporal table ends and the next one begins sometimes overlap
            status_table=status_table.add(status_table_temp)

        # sum neuronal edges between nodes
        edges_count = Counter([(int(edge[0].split("-")[0]), int(edge[1].split("-")[0])) for edge in edges_set])


        print("Creating igraph network from FNS data.")

        g = igraph.Graph()

        for node in nodes_dict:
            g.add_vertex(node, color_init=node)

        for i, edge in enumerate(edges_count):
            g.add_edge(edge[0], edge[1])

        # Define colormap. Explore with: fig = px.colors.qualitative.swatches(); fig.show().
        cmap = px.colors.qualitative.Pastel1

        print("Defining node positions with %s's algorithm" % layout)
        pos = g.layout(layout)
        for i in range(len(g.vs)):
            g.vs[i]["X"] = pos[i][0]
            g.vs[i]["Y"] = pos[i][1]

        # edges weights is the number of connections form a node to another. Used to define edges width. Normalized.
        edges_weight = [edge[1] for edge in edges_count]
        edges_weight = [(((count - np.min(edges_weight)) / np.std(edges_weight)) + 0.1) * 2 for count in edges_weight]

        # Calculate node indegrees
        indegree = list()
        for node in range(nodes):
            g.vs[node]["indegree"] = sum([edge[1] for edge in edges_count if edge[1] == node])
            indegree.append(sum([edge[1] for edge in edges_count if edge[1] == node]))
        # Normalizing indegrees to use them as node size
        indegree_norm = [(((d - np.min(indegree)) / np.std(indegree)) + 5) * 10 if d != 0 else 50 for d in indegree]

        print("PLOTTING")
        timestamp = datetime.now()
        new_dir= abstraction + "Viz" + timestamp.strftime("d%d_%m_%Y-t%H_%M_%S")
        os.mkdir(new_dir)

        fig = go.Figure()

        # Insert edges in the plot if needed
        if show_edges == "auto":
            if len(edges_count) <= 1000:  # Maximum add of 2 minutes.
                show_edges = "True"

        if show_edges == "True":
            print(" - Plotting edges: True")
            # Inserting edges in the plot
            eX = [(g.vs.find(edge[0])["X"], g.vs.find(edge[1])["X"]) for edge in edges_count]
            eY = [(g.vs.find(edge[0])["Y"], g.vs.find(edge[1])["Y"]) for edge in edges_count]
            for edge in range(len(edges_count)):
                edge_trace = go.Scatter(x=eX[edge], y=eY[edge], opacity=0.4, mode="lines",
                                        line=dict(color='rgb(210,210,210)', width=edges_weight[edge]))
                fig.add_trace(edge_trace)

        # Inserting nodes in the plot
        nX = np.asarray(pos)[:, 0]
        nY = np.asarray(pos)[:, 1]

        print("Drawing initial graph") # Graph at t=0, will show colorful nodes
        color = [node["color_init"] for node in g.vs]
        text = [["Node: " + str(node["name"]) + "<br>In_degree: " + str(node["indegree"])] for node in g.vs]
        node_trace = go.Scatter(x=nX, y=nY, opacity=0.9, mode='markers',
                                marker=dict(symbol='circle', size=20, color=color),
                                hoverinfo="text", hovertext=text)
        fig.add_trace(node_trace)
        fig.update_layout(template="plotly_white", title="Initialization", showlegend=False)
        pio.write_image(fig,new_dir + "/plotlyINIT.png",
                        format="png", width=800, height=800)

        # Prepare dynamical plots traces
        fig.update_traces(marker=dict(symbol='circle', size=20, line=dict(width=0.4, color="white"),
                                      color=color, colorbar=dict(thickness=12, title="Burning<br> events"),
                                      colorscale="Viridis",
                                      cmin=0,
                                      cmax=np.asmatrix(status_table.iloc[:, :-1]).max()),
                          selector=dict(type="scatter", mode="markers"))
        # Save a plot per timestep in status table
        for t in status_table:
            print("Drawing dynamic activity graphs - t: " + str(int(t)) + "/" + str(len(status_table.columns)), end="\r")
            color = status_table[t]
            fig.update_traces(marker=dict(color=color), selector=dict(type="scatter", mode="markers"))
            fig.update_layout(template="plotly_white", title="Dynamics - time: " + str(t) + " ms",
                              showlegend=False)
            pio.write_image(fig, new_dir + "/plotly" + str(t) + ".png",
                            format="png", width=800, height=800)

        print("Writing video from plots")
        fourcc = cv2.VideoWriter_fourcc(*'XVID')
        out = cv2.VideoWriter(new_dir+"/v_"+new_dir + ".avi",
                              fourcc, 3.0, (800, 800))
        image_list = glob.glob(new_dir + "/*.png")
        sorted_images = sorted(image_list, key=os.path.getmtime)
        for file in sorted_images:
            image_frame = cv2.imread(file)
            out.write(image_frame)
        out.release()

        print("Visualization at nodes' level in python was done: result in script's folder.")


def detailviz(data_path, nodes, simtime, npn=None, layout="fr", show_edges="auto", resolution=1):
    """
    This function executes two loops, the first one to gather information
    about edges in the network, the second one to gather neurons' activity
    in status table.

    :param data_path: path to FNS simulated data (must be the MATLAB compliant
    version).
    :param nodes: number of nodes in the simulation by FNS definition of
    node (i.e. region).
    :param simtime: Simulation length in milliseconds
    :param layout: gephi graph layout; def="fr", "kk", ..
    :param show_edges: True to plot edges. We dont
    recommend plotting large amount of edges. It slows down dramatically
    the computation.
    :param resolution: number of plots per millisecond
    :param npn: number of neurons per node passed in expert mode
    :return:
    """

    receivers = set() # i.e. neurons; senders can be external inputs
    edges_set = set()

    nodes_dict = dict()
    # Ask number of neurons per node
    if npn:
        for node in range(nodes):
            nodes_dict[node] = npn
            receivers.update([str(node) + "-" + str(neuron) for neuron in range(nodes_dict[node])])

    elif input("Every node has same number of neurons? (y/n)") == "y":
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
    df = pd.read_csv(data_path, header=None, usecols=[0, 1, 2, 3, 4, 5], chunksize=500000)
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
    new_dir = abstraction + "Viz" + timestamp.strftime("d%d_%m_%Y-t%H_%M_%S")
    os.mkdir(new_dir)


    print("\nCreating igraph network:")
    delay = 1 / 300 * len(edges_set) / 60 # 300 edges per second
    if input("With %i edges the process will take %0.2fm approx. Do you want to procced? (y/n)"
             % (len(edges_set), delay)) == "n":
        print("We recommend to try nodes visualization approach. Bye!")
        quit()

    g = igraph.Graph()

    for node in receivers:
        g.add_vertex(node, node=int(node[0]), neuron=int(node[2:]), color_init=node)

    for edge in edges_set:
        g.add_edge(edge[0], edge[1])

    # Define colormap for initial plot. Explore colors with: fig = px.colors.qualitative.swatches(); fig.show().
    cmap = px.colors.qualitative.Pastel1

    # Define node positions
    print("Defining node positions with %s's force-directed algorithm. May take a while." % layout)
    pos = g.layout(layout)
    for i in range(len(g.vs)):
        g.vs[i]["X"] = pos[i][0]
        g.vs[i]["Y"] = pos[i][1]

    # Calculate node degrees
    indegree = list()
    for i in range(len(receivers)):
        g.vs[i]["indegree"] = g.vs[i].indegree()
        indegree.append(g.vs[i].indegree())

    print("PLOTTING")
    fig = go.Figure()

    if show_edges == "auto":
        if len(edges_set) <= 500:  # More will take excessive time
            show_edges = "True"

    if show_edges == "True":
        print(" - Plotting edges: True"
              "Remember that 4000 edges takes 6s per plot; 8000 edges takes 20s."
              "No more than 500 edges are recommended to be plotted.")
        if input("Number of edges = " + str(len(edges_set))+".  Do you want to proceed? (y/n)") == "n":
            exit()
        # inserting edges in plot.
        eX = [(g.vs.find(edge[0])["X"], g.vs.find(edge[1])["X"]) for edge in edges_set]
        eY = [(g.vs.find(edge[0])["Y"], g.vs.find(edge[1])["Y"]) for edge in edges_set]
        for edge in range(len(edges_set)):
            print("Adding edge: " + str(edge)+"/"+str(len(edges_set)), end="\r")
            edge_trace = go.Scatter(x=eX[edge], y=eY[edge], opacity=0.6,
                                    mode="lines", line=dict(color='rgb(210,210,210)', width=0.4))
            fig.add_trace(edge_trace)
        print("Adding edge: " + str(edge)+"/"+str(len(edges_set)))

    # When too many edges, select inter-node edges
    elif show_edges!="True":
        edges_subset=[edge for edge in edges_set if edge[0][0]!=edge[1][0]]
        if len(edges_subset)>500:
            # select 500 random inter-node edges
            edges_subset = random.sample(edges_subset, 500)
        # inserting edges in plot.
        eX = [(g.vs.find(edge[0])["X"], g.vs.find(edge[1])["X"]) for edge in edges_subset]
        eY = [(g.vs.find(edge[0])["Y"], g.vs.find(edge[1])["Y"]) for edge in edges_subset]
        for edge in range(len(edges_subset)):
            print("Adding edge: " + str(edge)+"/"+str(len(edges_subset)), end="\r")
            edge_trace = go.Scatter(x=eX[edge], y=eY[edge], opacity=0.9,
                                    mode="lines", line=dict(color='rgb(210,210,210)', width=0.8))
            fig.add_trace(edge_trace)
        print("Adding edge: " + str(edge+1)+"/"+str(len(edges_subset)))

    # inserting nodes to the plot
    nX = np.asarray(pos)[:, 0]
    nY = np.asarray(pos)[:, 1]

    print("Drawing initial graph")
    color = [node["node"] for node in g.vs]
    text = [["Node: " + str(node["node"]) + "<br>In_degree: " + str(node["indegree"])] for node in g.vs]
    node_trace = go.Scatter(x=nX, y=nY, opacity=0.9, mode='markers',
                            marker=dict(symbol='circle', size=15, color=color, colorscale=cmap[:nodes]),
                            hoverinfo="text", hovertext=text)
    fig.add_trace(node_trace)
    fig.update_layout(template="plotly_white", title="Initialization", showlegend=False)
    pio.write_image(fig, new_dir + "/plotlyINIT.png",
                    format="png", width=700, height=700)

    # Prepare dynamical plots traces
    fig.update_traces(marker=dict(symbol='circle', size=15, opacity=0.6, line=dict(width=0.4, color="white"),
                                  color=color, colorbar=dict(thickness=12, title="Burning<br> events"),
                                  colorscale="Viridis",
                                  cmin=0,
                                  cmax=np.asmatrix(status_table.iloc[:, :-1]).max()),
                      selector=dict(type="scatter", mode="markers"))

    for t in status_table.iloc[:, :-1]:
        print("Drawing dynamic activity graphs - t: " + str(t) + "/" + str(len(status_table.columns)), end="\r")
        color = status_table[t]
        fig.update_traces(marker=dict(color=color), selector=dict(type="scatter", mode="markers"))
        fig.update_layout(template="plotly_white", title="Dynamics - time: " + str(t) + " ms", showlegend=False)
        pio.write_image(fig, new_dir + "/plotly" + str(t) + ".png", format="png", width=700, height=700)

    print("Writing a video from graph plots")
    fourcc = cv2.VideoWriter_fourcc(*'XVID')
    out = cv2.VideoWriter(new_dir+"/v_"+new_dir+ ".avi", fourcc, 3.0, (700, 700))
    image_list = glob.glob(new_dir + "/*.png")
    sorted_images = sorted(image_list, key=os.path.getmtime)
    for file in sorted_images:
        image_frame = cv2.imread(file)
        out.write(image_frame)
    out.release()

    print("Visualization at neuronal level in python was done: result in script's folder.")


mode = input("""
Welcome to FNS visualization in python.

Explore what's happening in FNS simulations.

First time? -> type: "more"; Expert? -> "e". 
Otherwise press enter.
""")

if mode == "more":
    print(init.__doc__)
    input("If you need more help contact us. Press enter to continue.")

data_path, abstraction, simtime, nodes, layout, show_edges, npn = init(mode)

if abstraction == "node":
    macroviz(data_path, nodes, simtime, npn, layout, show_edges)

elif abstraction == "neuron":
    detailviz(data_path, nodes, simtime, npn, layout, show_edges)
