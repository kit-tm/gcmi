package com.github.sherter.jcon.examples.generic_attributes.topology;

import com.github.sherter.jcon.InterceptableForwarder;
import org.projectfloodlight.openflow.types.DatapathId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Set;

/**
 * TopologyManager responsible for the mapping of DatapathId and TCP connections.
 */
public class TopologyManager {
    private static final Logger logger = LoggerFactory.getLogger(TopologyManager.class);

    private static TopologyManager instance;

    private HashMap<DatapathId, InterceptableForwarder.Context> dataPathToContext = new HashMap<>();
    private HashMap<InterceptableForwarder.Context, DatapathId> contextToDataPath = new HashMap<>();

    public static TopologyManager getInstance() {
        if(instance == null) {
            instance = new TopologyManager();
        }

        return instance;
    }

    private TopologyManager() {
    }

    /**
     * New Connection was registered.
     * @param dataPathId
     * @param context
     */
    public void updateSwitchIds(DatapathId dataPathId, InterceptableForwarder.Context context) {
        if (dataPathToContext.containsKey(dataPathId)) {
            InterceptableForwarder.Context oldContext = dataPathToContext.get(dataPathId);
            contextToDataPath.remove(oldContext);

        }

        dataPathToContext.put(dataPathId, context);
        contextToDataPath.put(context, dataPathId);
    }

    public Set<InterceptableForwarder.Context> getContexts() {
        return contextToDataPath.keySet();
    }

    public InterceptableForwarder.Context getContext(DatapathId dataPathId) {
        return dataPathToContext.get(dataPathId);
    }

    public DatapathId getDataPathId(InterceptableForwarder.Context context) {
        return contextToDataPath.get(context);
    }
}
