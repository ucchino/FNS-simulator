/**
 * "FNS" (Firnet NeuroScience)
 *
 * FNS is an event-driven Spiking Neural Network framework, oriented
 * to data-driven neural simulations.
 *
 * For contacts, description, citation to acclude, licensing, please
 * read the file enclosed with code: README
 */

package it.ppu.utils.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.lang.IgniteBiPredicate;

public class IgniteExample {
    
    public static void main(String[] args) {
        // Configurazione di Ignite
        IgniteConfiguration igniteCfg = new IgniteConfiguration();
        igniteCfg.setIgniteInstanceName("myIgniteInstance");

        // Configurazione della cache
        CacheConfiguration<Long, Spike> cacheCfg = new CacheConfiguration<>("spikeCache");
        cacheCfg.setIndexedTypes(Long.class, Spike.class);

        // Avvio di Ignite con la cache
        Ignition.start(igniteCfg).getOrCreateCache(cacheCfg);

        // Inserimento di oggetti di tipo Spike nella cache
        for (long i = 1; i <= 10; i++) {
            List<String> values = new ArrayList<>();
            values.add("value" + i);
            Spike spike = new Spike(values);
            Ignition.ignite("myIgniteInstance").getOrCreateCache("spikeCache").put(i, spike);
        }

        // Recupero di oggetti di tipo Spike ordinati per chiave
        IgniteBiPredicate<Long, Spike> filter = (k, v) -> true;
        ScanQuery<Long, Spike> scanQuery = new ScanQuery<>(filter);
        QueryCursor<List<?>> cursor = Ignition.ignite("myIgniteInstance")
            .cache("spikeCache")
            .query(new SqlFieldsQuery("SELECT * FROM Spike ORDER BY _key ASC"));
        
        for (List<?> row : cursor) {
            System.out.println(row.get(0) + ": " + row.get(1));
        }

        // Spegnimento di Ignite
        Ignition.stop("myIgniteInstance", true);
    }
}

class Spike {
    private List<String> values;

    public Spike(List<String> values) {
        this.values = values;
    }

    public List<String> getValues() {
        return Collections.unmodifiableList(values);
    }

    @Override
    public String toString() {
        return "Spike{" +
            "values=" + values +
            '}';
    }
}
