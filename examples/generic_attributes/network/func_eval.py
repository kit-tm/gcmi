from mininet.topo import Topo
from mininet.net import Mininet
from mininet.log import setLogLevel
from mininet.node import Controller, RemoteController
from mininet.cli import CLI

class TagTopo( Topo ):

  def __init__( self ):
    Topo.__init__( self )

    # outside hosts
    oh1 = self.addHost('oh1', ip='98.3.0.1', mac='00:00:00:00:00:01')
    oh2 = self.addHost('oh2', ip='98.3.0.2', mac='00:00:00:00:00:02')
    oh3 = self.addHost('oh3', ip='98.3.0.3', mac='00:00:00:00:00:03')
    oh4 = self.addHost('oh4', ip='98.3.0.4', mac='00:00:00:00:00:04')

    # inside hosts
    ih1 = self.addHost('ih1', ip='98.3.0.5', mac='00:00:00:00:00:05')
    ih2 = self.addHost('ih2', ip='98.3.0.6', mac='00:00:00:00:00:06')
    ih3 = self.addHost('ih3', ip='98.3.0.7', mac='00:00:00:00:00:07')
    ih4 = self.addHost('ih4', ip='98.3.0.8', mac='00:00:00:00:00:08')
    ih5 = self.addHost('ih5', ip='98.3.0.9', mac='00:00:00:00:00:09')

    # switches
    ingressSwitch = self.addSwitch( 's1', dpid='1' )
    diffSwitch = self.addSwitch( 's2', dpid='2' )
    mBoxFSwitch = self.addSwitch( 's3', dpid='3' )
    mBoxSwitch = self.addSwitch( 's4', dpid='4' )
    serverSwitch = self.addSwitch( 's5', dpid='5' )

    # outside host links
    self.addLink(oh1, ingressSwitch)
    self.addLink(oh2, ingressSwitch)
    self.addLink(oh3, ingressSwitch)
    self.addLink(oh4, ingressSwitch)

    # inside host links
    self.addLink(ih1, diffSwitch)
    self.addLink(ih2, serverSwitch)
    self.addLink(ih3, serverSwitch)
    self.addLink(ih4, serverSwitch)
    self.addLink(ih5, serverSwitch)

    # switch links
    self.addLink(ingressSwitch, diffSwitch)
    self.addLink(diffSwitch, mBoxFSwitch)
    self.addLink(mBoxFSwitch, mBoxSwitch)
    self.addLink(mBoxFSwitch, mBoxSwitch)
    self.addLink(mBoxFSwitch, serverSwitch)
    self.addLink(diffSwitch, serverSwitch)


setLogLevel( 'info' )
c1 = RemoteController( 'c1', ip='10.0.2.2', port=7473 )
net = Mininet (topo=TagTopo(), controller=c1, autoStaticArp=True)
net.start()
CLI(net)
net.stop()

