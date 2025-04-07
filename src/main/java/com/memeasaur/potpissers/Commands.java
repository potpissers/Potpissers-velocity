package com.memeasaur.potpissers;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;

import static com.memeasaur.potpissers.Listeners.proxy;

public class Commands implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        if (invocation.source() instanceof Player p) {
            switch (invocation.alias()) {
                case "hub" ->
                        handleServerSwitchCommand("hub", p);
                case "cubecore" ->
                        handleServerSwitchCommand("cubecore", p);
                case "kollusion" ->
                        handleServerSwitchCommand("kollusion", p);
                case "hcf", "hcfactions" ->
                        handleServerSwitchCommand("hcf", p);
                case "mz", "minez" ->
                        handleServerSwitchCommand("mz", p);
                case "mcsg" ->
                        handleServerSwitchCommand("mcsg", p);
                case "soup" ->
                        handleServerSwitchCommand("soup", p);
            }
        }
    }
    void handleServerSwitchCommand(String serverName, Player p) {
        proxy.getServer(serverName)
                .ifPresent(registeredServer -> p.createConnectionRequest(registeredServer).fireAndForget());
    }
}
