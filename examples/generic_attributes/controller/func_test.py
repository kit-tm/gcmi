from ryu.base import app_manager
from ryu.controller import ofp_event
from ryu.controller.handler import CONFIG_DISPATCHER, MAIN_DISPATCHER
from ryu.controller.handler import set_ev_cls
from ryu.ofproto import ofproto_v1_3
import time
import ipaddress

'''
    Parsing Configuration.
'''
class AttributeConfiguration:
    def __init__(self, data):
        self.app_id = int.from_bytes(data[0:16], byteorder='big', signed=False)
        self.app_id_length = data[16]

    def get_tag(self, tag_address):
        tag_value = int(ipaddress.IPv6Address(tag_address))
        tag_value <<= self.app_id_length
        tag_value += self.app_id

        return tag_value

    def get_tag_mask(self, tag_address, mask_address):
        mask = 2 ** self.app_id_length - 1

        tag_value = self.get_tag(tag_address)

        mask_value = int(ipaddress.IPv6Address(mask_address))
        mask_value <<= self.app_id_length
        mask_value += mask

        return tag_value, mask_value


'''
    Actual App for the functional test.
'''
class AttributeApp(app_manager.RyuApp):
    OFP_VERSIONS = [ofproto_v1_3.OFP_VERSION]

    def __init__(self, *args, **kwargs):
        super(AttributeApp, self).__init__(*args, **kwargs)
        self.mac_to_port = {}
        self.configuration = None
        self.switches = {}
        self.counter = 0

    @set_ev_cls(ofp_event.EventOFPErrorMsg, CONFIG_DISPATCHER)
    def error_received(self, ev):
        print("error {}".format(ev.msg))

    @set_ev_cls(ofp_event.EventOFPSwitchFeatures, CONFIG_DISPATCHER)
    def switch_features_handler(self, ev):
        datapath = ev.msg.datapath

        ofproto = datapath.ofproto
        parser = datapath.ofproto_parser
        match = parser.OFPMatch()
        actions = [parser.OFPActionOutput(ofproto.OFPP_CONTROLLER,
                                          ofproto.OFPCML_NO_BUFFER)]
        self.add_flow(datapath, 0, match, actions)

        self.switches[datapath.id] = datapath

        if self.counter > 3:
            eth_type = 2048
            msg = parser.OFPExperimenter(datapath, experimenter=6035141, exp_type=1, data=eth_type.to_bytes(2, byteorder = 'big'))
            self.logger.info("Sending experimenter! {}".format(msg))
            datapath.send_msg(msg)

        self.counter += 1

    @set_ev_cls(ofp_event.EventOFPExperimenter, CONFIG_DISPATCHER)
    def experimenter_message_handler(self, ev):
        msg = ev.msg
        datapath = ev.msg.datapath

        parser = datapath.ofproto_parser

        # tag reconfiguration requests
        self.use_src = parser.OFPExperimenter(datapath, experimenter=6035141, exp_type=3,
                                     data=b'\x01\x00')
        self.use_dst = parser.OFPExperimenter(datapath, experimenter=6035141, exp_type=3,
                                     data=b'\x01\x01')
        self.disable_src = parser.OFPExperimenter(datapath, experimenter=6035141, exp_type=3,
                                     data=b'\x00\x00')
        self.disable_dst = parser.OFPExperimenter(datapath, experimenter=6035141, exp_type=3,
                                     data=b'\x00\x01')

        if msg.experimenter == 6035141:
            print("Configuration received!")

            configuration = AttributeConfiguration(msg.data)

            print("App id is {}".format(configuration.app_id))
            print("App id length is {}".format(configuration.app_id_length))
            self.configuration = configuration

            self.install_rules()

            '''
            Provoking reassignment
            
            time.sleep(10)
            print("Start packet processing now!")
            time.sleep(5)
            self.provoke_reassignment(ev, True)
            time.sleep(5)

            for i in range(0, 20):
                self.provoke_reassignment(ev, i % 2 == 0)
            '''

    '''
        Test for reassigning tags and influence on in-flight packets.
    '''
    def provoke_reassignment(self, ev, disable_dst):
        datapath = ev.msg.datapath

        if disable_dst:
            print("Disable Eth Dst!")
            datapath.send_msg(self.use_src)
            datapath.send_msg(self.disable_dst)
        else:
            print("Disable Eth Src!")
            datapath.send_msg(self.use_dst)
            datapath.send_msg(self.disable_src)

    def get_tag(self, value):
        return self.configuration.get_tag(value)

    def get_tag_mask(self, value, mask):
        return self.configuration.get_tag_mask(value, mask)

    def install_rules(self):
        self.install_ingress()
        self.install_diff()
        self.install_dis()
        self.install_firewall()
        self.install_mbox()

    def install_ingress(self):
        datapath = self.switches[1]
        parser = datapath.ofproto_parser

        ### right direction ###
        match = parser.OFPMatch(ipv4_src="98.3.0.1")
        actions = [parser.OFPActionSetField(gen_tag=self.get_tag(2)), parser.OFPActionOutput(5)]
        self.add_flow(datapath, 3, match, actions)

        match = parser.OFPMatch(ipv4_src="98.3.0.2")
        actions = [parser.OFPActionSetField(gen_tag=self.get_tag(4)), parser.OFPActionOutput(5)]
        self.add_flow(datapath, 3, match, actions)

        match = parser.OFPMatch(ipv4_src="98.3.0.3")
        actions = [parser.OFPActionSetField(gen_tag=self.get_tag(5)), parser.OFPActionOutput(5)]
        self.add_flow(datapath, 3, match, actions)

        match = parser.OFPMatch(ipv4_src="98.3.0.4")
        actions = [parser.OFPActionSetField(gen_tag=self.get_tag(13)), parser.OFPActionOutput(5)]
        self.add_flow(datapath, 3, match, actions)

        ### left direction ###
        match = parser.OFPMatch(in_port=5, ipv4_dst="98.3.0.1")
        actions = [parser.OFPActionOutput(1)]
        self.add_flow(datapath, 3, match, actions)

        match = parser.OFPMatch(in_port=5, ipv4_dst="98.3.0.2")
        actions = [parser.OFPActionOutput(2)]
        self.add_flow(datapath, 3, match, actions)

        match = parser.OFPMatch(in_port=5, ipv4_dst="98.3.0.3")
        actions = [parser.OFPActionOutput(3)]
        self.add_flow(datapath, 3, match, actions)

        match = parser.OFPMatch(in_port=5, ipv4_dst="98.3.0.4")
        actions = [parser.OFPActionOutput(4)]
        self.add_flow(datapath, 3, match, actions)

    def install_diff(self):
        datapath = self.switches[2]
        parser = datapath.ofproto_parser

        match = parser.OFPMatch(gen_tag=self.get_tag_mask(1, 1))
        actions = [parser.OFPActionOutput(3)]
        self.add_flow(datapath, 3, match, actions)

        match = parser.OFPMatch(gen_tag=self.get_tag_mask(2, 2))
        actions = [parser.OFPActionOutput(1)]
        self.add_flow(datapath, 3, match, actions)

        match = parser.OFPMatch(gen_tag=self.get_tag_mask(0, 3))
        actions = [parser.OFPActionOutput(4)]
        self.add_flow(datapath, 3, match, actions)

        match = parser.OFPMatch(in_port=4)
        actions = [parser.OFPActionOutput(2)]
        self.add_flow(datapath, 20, match, actions)

        match = parser.OFPMatch(in_port=1)
        actions = [parser.OFPActionOutput(2)]
        self.add_flow(datapath, 3, match, actions)

    def install_mbox(self):
        datapath = self.switches[3]
        parser = datapath.ofproto_parser

        match = parser.OFPMatch(in_port=1)
        actions = [parser.OFPActionOutput(2)]
        self.add_flow(datapath, 3, match, actions)

        match = parser.OFPMatch(in_port=3)
        actions = [parser.OFPActionOutput(4)]
        self.add_flow(datapath, 3, match, actions)

    def install_firewall(self):
        datapath = self.switches[4]
        parser = datapath.ofproto_parser

        match = parser.OFPMatch(ipv4_src="98.3.0.4")
        actions = [parser.OFPActionOutput(2)]
        self.add_flow(datapath, 3, match, actions)


    def install_dis(self):
        datapath = self.switches[5]
        parser = datapath.ofproto_parser

        match = parser.OFPMatch(gen_tag=self.get_tag_mask(0, 12))
        actions = [parser.OFPActionOutput(1)]
        self.add_flow(datapath, 3, match, actions)

        match = parser.OFPMatch(gen_tag=self.get_tag_mask(4, 12))
        actions = [parser.OFPActionOutput(2)]
        self.add_flow(datapath, 3, match, actions)

        match = parser.OFPMatch(gen_tag=self.get_tag_mask(8, 12))
        actions = [parser.OFPActionOutput(3)]
        self.add_flow(datapath, 3, match, actions)

        match = parser.OFPMatch(gen_tag=self.get_tag_mask(12, 12))
        actions = [parser.OFPActionOutput(4)]
        self.add_flow(datapath, 3, match, actions)

        self.add_flow(datapath, 20, parser.OFPMatch(in_port=1), [parser.OFPActionOutput(6)])
        self.add_flow(datapath, 20, parser.OFPMatch(in_port=2), [parser.OFPActionOutput(6)])
        self.add_flow(datapath, 20, parser.OFPMatch(in_port=3), [parser.OFPActionOutput(6)])
        self.add_flow(datapath, 20, parser.OFPMatch(in_port=4), [parser.OFPActionOutput(6)])

    def add_flow(self, datapath, priority, match, actions, buffer_id=None):
        ofproto = datapath.ofproto
        parser = datapath.ofproto_parser

        inst = [parser.OFPInstructionActions(ofproto.OFPIT_APPLY_ACTIONS,
                                             actions)]
        if buffer_id:
            mod = parser.OFPFlowMod(datapath=datapath, buffer_id=buffer_id,
                                    priority=priority, match=match,
                                    instructions=inst)
        else:
            mod = parser.OFPFlowMod(datapath=datapath, priority=priority,
                                    match=match, instructions=inst)

        datapath.send_msg(mod)

