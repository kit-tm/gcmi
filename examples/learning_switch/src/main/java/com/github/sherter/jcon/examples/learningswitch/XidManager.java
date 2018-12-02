package com.github.sherter.jcon.examples.learningswitch;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

class XidManager {
  private final AtomicLong counter = new AtomicLong();

  private final Map<Long, Long> upstreamMapping = new HashMap<>();
  private final Set<Long> ourXids = new HashSet<>();

  /**
   * for example, if this app has an outstanding request with this id, the upstream id needs to be
   * translated
   */
  boolean needsReplacement(long upstreamXid) {
    return ourXids.contains(upstreamXid);
  }

  long createForUs() {
    long newXid = counter.incrementAndGet();
    ourXids.add(newXid);
    return newXid;
  }

  boolean isReplyForUs(long xid) {
    return ourXids.contains(xid);
  }

  long createReplacement(long upstreamXid) {
    long newXid = counter.incrementAndGet();
    upstreamMapping.put(newXid, upstreamXid);
    return newXid;
  }

  boolean isReplacement(long xid) {
    return upstreamMapping.containsKey(xid);
  }

  long removeReplacement(long xid) {
    return upstreamMapping.remove(xid);
  }

  void forgetOurId(long xid) {
    ourXids.remove(xid);
  }
}
