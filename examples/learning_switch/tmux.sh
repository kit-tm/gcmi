#!/bin/dash

SESSION=learning_switch

tmux -2 new-session -d -s $SESSION

tmux send-keys "ryu run --ofp-tcp-listen-port 6634 log2.py &> log2.log &" C-m "less -S +F log2.log"
tmux split-window -v -p 66
tmux send-keys "build/install/learning_switch/bin/learning_switch --listen 0.0.0.0:6633 --upstream 127.0.0.1:6634 > learning_switch.log &" C-m "less -S +F learning_switch.log"
tmux split-window -v
tmux send-keys "sudo mn --controller=remote,port=6633"
tmux -2 attach-session -t $SESSION
