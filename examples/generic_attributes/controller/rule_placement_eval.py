from ryu.base import app_manager
from ryu.controller import ofp_event
from ryu.controller.handler import CONFIG_DISPATCHER, MAIN_DISPATCHER
from ryu.controller.handler import set_ev_cls
from ryu.ofproto import ofproto_v1_3
from timeit import default_timer as timer
import os
import csv

class RulePlacementEval(app_manager.RyuApp):
    OFP_VERSIONS = [ofproto_v1_3.OFP_VERSION]

    def __init__(self, *args, **kwargs):
        super(RulePlacementEval, self).__init__(*args, **kwargs)
        self.onlyOnce = True
        self.timer_start = 0
        self.name = ''
        self.num_rules = 0

    @set_ev_cls(ofp_event.EventOFPErrorMsg, CONFIG_DISPATCHER)
    def error_received(self, ev):
        print("error {}".format(ev.msg))

    @set_ev_cls(ofp_event.EventOFPSwitchFeatures, CONFIG_DISPATCHER)
    def switch_features_handler(self, ev):
        datapath = ev.msg.datapath

        if self.onlyOnce:
            self.install_rules(datapath)
            self.onlyOnce = False

    def install_rules(self, datapath):
        ofproto = datapath.ofproto
        parser = datapath.ofproto_parser

        self.num_rules = 1
        startValue = 1

        # name of scenario: none, TIL, tag_1, tag_3
        self.name = 'tag_3'
        to_send = []

        # creating messages
        for i in range(0, self.num_rules):
            tag = i + startValue
            match = parser.OFPMatch(in_port=1, gen_tag=tag)
            actions = [parser.OFPActionSetField(gen_tag=tag), parser.OFPActionOutput(0)]
            inst = [parser.OFPInstructionActions(ofproto.OFPIT_APPLY_ACTIONS,
                                             actions)]
            mod = parser.OFPFlowMod(datapath=datapath, priority=3,
                                    match=match, instructions=inst)
            to_send.append(mod)

        self.timer_start = timer()

        # sending messages
        for i in range(0, self.num_rules):
            datapath.send_msg(to_send[i])

        ofp_parser = datapath.ofproto_parser

        req = ofp_parser.OFPBarrierRequest(datapath)
        datapath.send_msg(req)


    @set_ev_cls(ofp_event.EventOFPBarrierReply, MAIN_DISPATCHER)
    def barrier_reply_handler(self, ev):
        self.logger.debug('OFPBarrierReply received')
        timer_end = timer()

        elapsed = timer_end - self.timer_start

        self.logger.info('Elapsed time: {}s'.format(elapsed))

        '''
        Writing into file.
        
        folder_name = "/home/david/Documents/ma-koerver-thesis/thesis/eval/z_rule_time"
        
        if not os.path.exists(folder_name):
            os.makedirs(folder_name)
        
        name = folder_name + '/{}.csv'.format(self.name)
        try:
            file = open(name, 'r')
        except FileNotFoundError:
            with open(name, 'a') as file:
                writer = csv.writer(file)
                writer.writerow(['num_rules','time'])

        with open(name, 'a') as file:
            writer = csv.writer(file)
            writer.writerow([self.num_rules, elapsed * 1000])'''
