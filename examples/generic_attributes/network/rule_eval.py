from mininet.topo import Topo
from mininet.net import Mininet
from mininet.log import setLogLevel
from mininet.node import Controller, RemoteController
from mininet.cli import CLI

class RuleEvalTopo( Topo ):

  def __init__( self ):
    Topo.__init__( self )

    switch = self.addSwitch( 's1', dpid='1' )


setLogLevel( 'info' )
c1 = RemoteController( 'c1', ip='10.0.2.2', port=7473 )
net = Mininet (topo=RuleEvalTopo(), controller=c1, autoStaticArp=True)
net.start()
CLI(net)
net.stop()

