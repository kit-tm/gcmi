package com.github.sherter.jcon.examples.generic_attributes.interception.eval;

import com.github.sherter.jcon.XidManager;
import com.github.sherter.jcon.examples.generic_attributes.topology.TopologyManager;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.ver13.OFFactoryVer13;
import org.projectfloodlight.openflow.types.DatapathId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Benchmarker to measure the time of flow rule placement on a number of switches.
 */
public class BarrierMeasurer {
    private static final Logger logger = LoggerFactory.getLogger(BarrierMeasurer.class);
    private static OFFactoryVer13 factory = (OFFactoryVer13) OFFactories.getFactory(OFVersion.OF_13);
    private static BarrierMeasurer instance = null;
    private List<DatapathId> datapathIds;
    private XidManager xidManager;
    private long startTime;
    private Barrier barrier;
    private String purpose;

    public BarrierMeasurer() {
        barrier = new Barrier();
    }

    public static BarrierMeasurer getInstance() {
        if (instance == null)
            instance = new BarrierMeasurer();

        return instance;
    }

    public Barrier getBarrier() {
        return barrier;
    }

    public void setXidManager(XidManager xidManager) {
        this.xidManager = xidManager;
    }

    /**
     * Starting the measurement for a number of switches.
     * @param purpose
     * @param datapathIds
     */
    public void startMeasuring(String purpose, List<DatapathId> datapathIds) {
        this.purpose = purpose;
        this.datapathIds = datapathIds;

        logger.info("Starting BarrierMeasurer with ids: " + datapathIds);
        startTime = System.currentTimeMillis();

    }


    /**
     * Ending the measurement for a number of switches.
     * Sending BarrierRequest that will be answered by a BarrierReply.
     */
    public void endMeasuring() {
        ArrayList<Long> xids = new ArrayList<>();

        for (DatapathId datapathId : datapathIds) {
            long xid = xidManager.newOriginXid();
            xids.add(xid);

            logger.warn("send downstream with xid " + xid);

            TopologyManager.getInstance().getContext(datapathId)
                    .sendDownstream(factory.barrierRequest().createBuilder().setXid(xid).build());

        }

        barrier.addInformation(purpose, startTime, xids);
    }

    /**
     * Outputting the measured time.
     */
    public static class Barrier {
        private List<Long> xids = null;
        private String purpose;
        private long startTime;

        void addInformation(String purpose, long startTime, List<Long> xids) {
            this.xids = xids;
            this.purpose = purpose;
            this.startTime = startTime;
        }

        /**
         * Received BarrierReply message.
         * @param xid
         * @return
         */
        public boolean responseReceived(long xid) {
            if (xids != null) {
                boolean containsXid = xids.contains(xid);
                xids.remove(xid);

                if (xids.size() == 0 && containsXid) {
                    long time = System.currentTimeMillis() - startTime;
                    logger.info("time " + time);
                    ConflictEvaluationWriter.getInstance().addConflictTime(time);
                }


                return containsXid;
            } else {
                return false;
            }
        }
    }

}
