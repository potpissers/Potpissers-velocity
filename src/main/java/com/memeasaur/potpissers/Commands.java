package com.memeasaur.potpissers;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;

import java.nio.charset.StandardCharsets;

import static com.memeasaur.potpissers.Listeners.HCF_REVIVER;
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
                case "hcf", "hcfactions" -> {
                    switch (invocation.arguments().length) {
                        case 0 ->
                                handleServerSwitchCommand("hcf", p);
                        case 1 -> {
                            switch (invocation.arguments()[0]) {
                                case "revive", "combatrevive", "pvprevive", "resurrect" ->
                                        proxy.getServer("hcf").ifPresent(
                                                server -> server.sendPluginMessage(HCF_REVIVER, (p.getUsername() + '|' + p.getUsername()).getBytes(StandardCharsets.UTF_8))
                                        );
                                default -> {
                                    p.sendMessage(Component.text("invalid (args)"));
                                    return;
                                }
                            }
                        }
                        case 2 -> {
                            switch (invocation.arguments()[0]) {
                                case "revive", "combatrevive", "pvprevive", "resurrect" -> {
                                    proxy.getServer("hcf").ifPresent(
                                            server -> server.sendPluginMessage(HCF_REVIVER, (p.getUsername() + '|' + invocation.arguments()[1]).getBytes(StandardCharsets.UTF_8))
                                    );
                                }
                                default -> {
                                    p.sendMessage(Component.text("invalid (args)"));
                                    return;
                                }
                            }
                        }
                    }
                }
                case "mz", "minez" ->
                        handleServerSwitchCommand("minez", p);
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
