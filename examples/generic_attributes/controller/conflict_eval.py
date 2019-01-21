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

from ryu.base import app_manager
from ryu.controller import ofp_event
from ryu.controller.handler import CONFIG_DISPATCHER, MAIN_DISPATCHER
from ryu.controller.handler import set_ev_cls
from ryu.ofproto import ofproto_v1_3
from timeit import default_timer as timer
import os
import csv


class ConflictResolutionEval(app_manager.RyuApp):
    OFP_VERSIONS = [ofproto_v1_3.OFP_VERSION]

    def __init__(self, *args, **kwargs):
        super(ConflictResolutionEval, self).__init__(*args, **kwargs)
        self.onlyOnce = True
        self.timer_start = 0
        self.name = ''
        self.num_rules = 0
        self.datapath = None
        self.parser = None
        self.ofproto = None
        self.received_barriers = 0
        self.conflict_message = None

    @set_ev_cls(ofp_event.EventOFPErrorMsg, CONFIG_DISPATCHER)
    def error_received(self, ev):
        print("error {}".format(ev.msg))

    @set_ev_cls(ofp_event.EventOFPSwitchFeatures, CONFIG_DISPATCHER)
    def switch_features_handler(self, ev):
        self.datapath = ev.msg.datapath
        self.ofproto = self.datapath.ofproto
        self.parser = self.datapath.ofproto_parser

        match = self.parser.OFPMatch(in_port=1, eth_dst=0)
        actions = [self.parser.OFPActionSetField(eth_dst=0), self.parser.OFPActionOutput(0)]
        inst = [self.parser.OFPInstructionActions(self.ofproto.OFPIT_APPLY_ACTIONS,
                                                  actions)]
        self.conflict_message = self.parser.OFPFlowMod(datapath=self.datapath, priority=3,
                                     match=match, instructions=inst)

        if self.onlyOnce:
            self.install_rules(self.datapath)
            self.onlyOnce = False

    def install_rules(self, datapath):
        ofproto = self.datapath.ofproto
        parser = self.datapath.ofproto_parser

        self.num_rules = 10000
        start_value = 1

        # installing rules.
        for i in range(0, self.num_rules):
            tag = i + start_value
            match = parser.OFPMatch(in_port=1, gen_tag=tag)
            actions = [parser.OFPActionSetField(gen_tag=tag), parser.OFPActionOutput(0)]
            inst = [parser.OFPInstructionActions(ofproto.OFPIT_APPLY_ACTIONS,
                                                 actions)]
            mod = parser.OFPFlowMod(datapath=datapath, priority=3,
                                    match=match, instructions=inst)
            datapath.send_msg(mod)

        ofp_parser = datapath.ofproto_parser

        # send barrier request.
        req = ofp_parser.OFPBarrierRequest(datapath)
        datapath.send_msg(req)

    @set_ev_cls(ofp_event.EventOFPBarrierReply, MAIN_DISPATCHER)
    def barrier_reply_handler(self, ev):
        # first barrier reply -> Send conflicting message.
        if self.received_barriers == 0:
            self.datapath.send_msg(self.conflict_message)
            self.datapath.send_msg(self.parser.OFPBarrierRequest(self.datapath))
            self.timer_start = timer()
            self.received_barriers += 1

        # second barrier reply -> Stop timer.
        else:
            timer_end = timer()
            elapsed = timer_end - self.timer_start
            self.logger.info('Elapsed time: {}s'.format(elapsed))

            '''
            Writing into file.
            
            folder_name = "/home/david/Documents/ma-koerver-thesis/thesis/eval/z_conflict"

            if not os.path.exists(folder_name):
                os.makedirs(folder_name)

            name = folder_name + '/{}.csv'.format('py_results')
            try:
                file = open(name, 'r')
            except FileNotFoundError:
                with open(name, 'a') as file:
                    writer = csv.writer(file)
                    writer.writerow(['num_rules','time'])

            with open(name, 'a') as file:
                writer = csv.writer(file)
                writer.writerow([self.num_rules, elapsed * 1000])
                '''

