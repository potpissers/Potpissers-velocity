package com.memeasaur.potpissers;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.scheduler.Scheduler;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static com.memeasaur.potpissers.Util.executeQueryOptionalRunnable;
import static com.memeasaur.potpissers.Util.getIpBytes;

@Plugin(
    id = "potpissers",
    name = "potpissers",
    version = "1.0-SNAPSHOT"
)
public class Listeners {
    public static ProxyServer proxy;
    public static Listeners plugin;
    public static Scheduler scheduler;
    @Inject
    public Listeners(ProxyServer proxy) {
        Listeners.proxy = proxy;
        Listeners.plugin = this;
        Listeners.scheduler = proxy.getScheduler();
    }
    public static final HikariDataSource PQ_POOL;
    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        HikariConfig postgresConfig = new HikariConfig();
        postgresConfig.setJdbcUrl(System.getenv("POSTGRES_JDBC_URL"));
        PQ_POOL = new HikariDataSource(postgresConfig);
    }
//    public static final String POSTGRES_AES_KEY = System.getenv("POSTGRES_AES_KEY"); TODO deprecate
    public static final ConcurrentHashMap<Player, OffsetDateTime> newPlayers = new ConcurrentHashMap<>();

    public static final MinecraftChannelIdentifier SERVER_SWITCHER = MinecraftChannelIdentifier.create("potpissers", "serverswitcher");

    public static final MinecraftChannelIdentifier PLAYER_MESSAGER = MinecraftChannelIdentifier.create("potpissers", "messager");

    public static final MinecraftChannelIdentifier PARTY_MESSAGER = MinecraftChannelIdentifier.create("potpissers", "partymessager");
    public static final MinecraftChannelIdentifier PARTY_UNALLIER = MinecraftChannelIdentifier.create("potpissers", "partyunallier");
    public static final MinecraftChannelIdentifier PARTY_UNENEMIER = MinecraftChannelIdentifier.create("potpissers", "partyunenemier");
    public static final MinecraftChannelIdentifier PARTY_ALLIER = MinecraftChannelIdentifier.create("potpissers", "partyallier");
    public static final MinecraftChannelIdentifier PARTY_ENEMIER = MinecraftChannelIdentifier.create("potpissers", "partyenemier");
    public static final MinecraftChannelIdentifier PARTY_DISBANDER = MinecraftChannelIdentifier.create("potpissers", "partydisbander");
    public static final MinecraftChannelIdentifier PARTY_KICKER = MinecraftChannelIdentifier.create("potpissers", "partykicker");

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        proxy.getChannelRegistrar().register(SERVER_SWITCHER);

        proxy.getChannelRegistrar().register(PLAYER_MESSAGER);

        proxy.getChannelRegistrar().register(PARTY_MESSAGER);
        proxy.getChannelRegistrar().register(PARTY_UNALLIER);
        proxy.getChannelRegistrar().register(PARTY_UNENEMIER);
        proxy.getChannelRegistrar().register(PARTY_ALLIER);
        proxy.getChannelRegistrar().register(PARTY_ENEMIER);
        proxy.getChannelRegistrar().register(PARTY_DISBANDER);
        proxy.getChannelRegistrar().register(PARTY_KICKER);

        SimpleCommand commands = new Commands();
        CommandManager commandManager = proxy.getCommandManager();
        for (String commandName : List.of("hub", "cubecore", "kollusion", "mcsg", "soup", "mz", "minez", "hcfactions", "hcf"))
            commandManager.register(commandManager.metaBuilder(commandName).plugin(this).build(), commands);
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent e) {
        if (e.getSource() instanceof ServerConnection serverConnection) {
            switch (e.getIdentifier().getId()) {
                case "potpissers:serverswitcher" ->
                    proxy.getServer(new String(e.getData(), StandardCharsets.UTF_8))
                            .ifPresent(registeredServer -> serverConnection.getPlayer().createConnectionRequest(registeredServer).fireAndForget());
                case "potpissers:messager" -> {
                    // TODO -> just message player
                    for (RegisteredServer registeredServer : proxy.getAllServers())
                        registeredServer.sendPluginMessage(PLAYER_MESSAGER, e.getData());
                }
                case "potpissers:partymessager" -> {
                    // TODO -> get members (?)
                    for (RegisteredServer registeredServer : proxy.getAllServers())
                        registeredServer.sendPluginMessage(PARTY_MESSAGER, e.getData());
                }
                case "potpissers:partyunallier" -> {
                    for (RegisteredServer registeredServer : proxy.getAllServers())
                        registeredServer.sendPluginMessage(PARTY_UNALLIER, e.getData());
                }
                case "potpissers:partyunenemier" -> {
                    for (RegisteredServer registeredServer : proxy.getAllServers())
                        registeredServer.sendPluginMessage(PARTY_UNENEMIER, e.getData());
                }
                case "potpissers:partyallier" -> {
                    for (RegisteredServer registeredServer : proxy.getAllServers())
                        registeredServer.sendPluginMessage(PARTY_ALLIER, e.getData());
                }
                case "potpissers:partyenemier" -> {
                    for (RegisteredServer registeredServer : proxy.getAllServers())
                        registeredServer.sendPluginMessage(PARTY_ENEMIER, e.getData());
                }
                case "potpissers:partydisbander" -> {
                    for (RegisteredServer registeredServer : proxy.getAllServers())
                        registeredServer.sendPluginMessage(PARTY_DISBANDER, e.getData());
                }
                case "potpissers:partykicker" -> {
                    for (RegisteredServer registeredServer : proxy.getAllServers())
                        registeredServer.sendPluginMessage(PARTY_KICKER, e.getData());
                }
            }
        }
    }
    @Subscribe
    public void onPlayerConnect(ServerPostConnectEvent e) {
        proxy.getScheduler().buildTask(plugin, () -> {
            try (Connection connection = PQ_POOL.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(RETURN_USER_REFERRAL_STAR)) {
                preparedStatement.setObject(1, e.getPlayer().getUniqueId());
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (!resultSet.next() && e.getPlayer() != null)
                        newPlayers.put(e.getPlayer(), OffsetDateTime.now());
                }
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }).schedule();
    }
    public static final String RETURN_USER_REFERRAL_STAR = """
            SELECT *
            FROM user_referrals
            WHERE user_uuid = ?""";
    @Subscribe
    public void onPlayerDisconnect(DisconnectEvent e) {
        Player player = e.getPlayer();
        if (newPlayers.containsKey(player)) // TODO -> network msg ?
            executeQueryOptionalRunnable("CALL handle_upsert_user_referral(?, ?, ?, ?)", new Object[]{player.getUniqueId(), newPlayers.get(player), getIpBytes(player.getRemoteAddress().getAddress().getHostAddress()), player.getUsername()}, () -> newPlayers.remove(player));
        executeQueryOptionalRunnable("CALL handle_delete_online_player(?)", new Object[]{player.getUniqueId()}, null);
    }
}
