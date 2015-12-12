firewall {
    all-ping enable
    broadcast-ping disable
    ipv6-receive-redirects disable
    ipv6-src-route disable
    ip-src-route disable
    log-martians enable
    modify P2P {
        description "Mark P2P Traffic"
        rule 10 {
            action modify
            modify {
                mark 1000
            }
            p2p {
                all
            }
        }
    }
    name WAN_IN {
        default-action drop
        description "WAN to internal"
        rule 10 {
            action accept
            description "Allow established/related"
            state {
                established enable
                related enable
            }
        }
        rule 20 {
            action drop
            description "Drop invalid state"
            state {
                invalid enable
            }
        }
    }
    name WAN_LOCAL {
        default-action accept
        description "WAN to router"
        rule 10 {
            action accept
            description "Allow established/related"
            state {
                established enable
                related enable
            }
        }
        rule 20 {
            action drop
            description "Drop invalid state"
            state {
                invalid enable
            }
        }
    }
    receive-redirects disable
    send-redirects enable
    source-validation disable
    syn-cookies enable
}
interfaces {
    ethernet eth0 {
        address dhcp
        description Internet
        duplex auto
        firewall {
            in {
                modify P2P
                name WAN_IN
            }
            local {
                name WAN_LOCAL
            }
        }
        speed auto
        traffic-policy {
            out VMedia-OUT
        }
    }
    ethernet eth1 {
        address 172.16.10.1/24
        description "Local 1"
        duplex auto
        firewall {
            in {
                modify P2P
            }
        }
        speed auto
        traffic-policy {
            out VMedia-IN
        }
    }
    ethernet eth2 {
        address 172.16.100.1/24
        description "Local 2"
        duplex auto
        speed auto
    }
    loopback lo {
    }
}
port-forward {
    auto-firewall enable
    hairpin-nat enable
    lan-interface eth1
    wan-interface eth0
}
service {
    dhcp-server {
        disabled false
        hostfile-update disable
        shared-network-name LAN1 {
            authoritative disable
            subnet 172.16.10.0/24 {
                default-router 172.16.10.1
                dns-server 172.16.10.1
                lease 1440
                start 172.16.10.100 {
                    stop 172.16.10.150
                }
            }
        }
    }
    dns {
        forwarding {
            cache-size 150
            listen-on eth1
        }
    }
    gui {
        https-port 443
    }
    nat {
        rule 5010 {
            description "masquerade for WAN"
            outbound-interface eth0
            type masquerade
        }
    }
    ssh {
        port 22
        protocol-version v2
    }
}
/* These are defaults, password is ubnt/ubnt */
system {
    host-name ubnt
    login {
        user ubnt {
            authentication {
                encrypted-password $1$zKNoUbAo$gomzUbYvgyUMcD436Wo66.
            }
            level admin
        }
    }
    ntp {
        server 0.ubnt.pool.ntp.org {
        }
        server 1.ubnt.pool.ntp.org {
        }
        server 2.ubnt.pool.ntp.org {
        }
        server 3.ubnt.pool.ntp.org {
        }
    }
    syslog {
        global {
            facility all {
                level notice
            }
            facility protocols {
                level debug
            }
        }
    }
    time-zone UTC
    traffic-analysis {
    }
}
traffic-control {
}
traffic-policy {
    shaper VMedia-IN {
        bandwidth 90mbit
        class 10 {
            bandwidth 15%
            burst 15k
            ceiling 75%
            description "Marked P2P traffic"
            match P2P {
                mark 1000
            }
            queue-type fair-queue
        }
        class 20 {
            bandwidth 15%
            burst 15k
            ceiling 30%
            description "TeamSpeak Traffic"
            match UDP_9987 {
                ip {
                    destination {
                        port 9987
                    }
                    protocol udp
                }
            }
            queue-type fair-queue
        }
        class 30 {
            bandwidth 2%
            burst 15k
            match ICMP {
                ip {
                    protocol icmp
                }
            }
            queue-type fair-queue
        }
        default {
            bandwidth 50%
            burst 15k
            ceiling 100%
            queue-type fair-queue
        }
        description "Download - From eth1 to LAN"
    }
    shaper VMedia-OUT {
        bandwidth 4.5mbit
        class 10 {
            bandwidth 10%
            burst 15k
            ceiling 30%
            description "Marked P2P traffic"
            match P2P {
                mark 1000
            }
            queue-type fair-queue
        }
        class 20 {
            bandwidth 15%
            burst 15k
            ceiling 30%
            description "TeamSpeak Traffic"
            match UDP_9987 {
                ip {
                    destination {
                        port 9987
                    }
                    protocol udp
                }
            }
            queue-type fair-queue
        }
        class 30 {
            bandwidth 2%
            burst 15k
            match ICMP {
                ip {
                    protocol icmp
                }
            }
            queue-type fair-queue
        }
        default {
            bandwidth 50%
            burst 15k
            ceiling 100%
            queue-type fair-queue
        }
        description "Upload - From eth0 to WAN"
    }
}


/* Warning: Do not remove the following line. */
/* === vyatta-config-version: "config-management@1:conntrack@1:cron@1:dhcp-relay@1:dhcp-server@4:firewall@5:ipsec@4:nat@3:qos@1:quagga@2:system@4:ubnt-pptp@1:ubnt-util@1:vrrp@1:webgui@1:webproxy@1:zone-policy@1" === */
/* Release version: v1.7.0.4783374.150622.1534 */
