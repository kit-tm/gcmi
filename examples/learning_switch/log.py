# Copyright (C) 2011 Nippon Telegraph and Telephone Corporation.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
# implied.
# See the License for the specific language governing permissions and
# limitations under the License.
"""
An OpenFlow 1.0 L2 learning switch implementation.
"""

from ryu.base import app_manager
from ryu.controller.ofp_event import (EventOFPMsgBase, EventOFPPacketIn)
from ryu.controller.handler import (CONFIG_DISPATCHER, DEAD_DISPATCHER,
                                    HANDSHAKE_DISPATCHER, MAIN_DISPATCHER,
                                    set_ev_cls)
from ryu.ofproto import ofproto_v1_3


class Logger(app_manager.RyuApp):
    OFP_VERSIONS = [ofproto_v1_3.OFP_VERSION]

    def __init__(self, *args, **kwargs):
        super(Logger, self).__init__(*args, **kwargs)

    @set_ev_cls(EventOFPMsgBase, [
        HANDSHAKE_DISPATCHER, CONFIG_DISPATCHER, MAIN_DISPATCHER,
        DEAD_DISPATCHER
    ])
    def message_handler(self, ev):
        self.logger.info(repr(ev.msg))
