package pl.extollite.autocleaner;

import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.item.EntityExpBottle;
import cn.nukkit.entity.item.EntityItem;
import cn.nukkit.entity.item.EntityXPOrb;
import cn.nukkit.entity.mob.EntityMob;
import cn.nukkit.entity.passive.EntityAnimal;
import cn.nukkit.entity.passive.EntityWaterAnimal;
import cn.nukkit.entity.projectile.EntityProjectile;
import cn.nukkit.event.Listener;
import cn.nukkit.level.Level;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.scheduler.TaskHandler;
import cn.nukkit.utils.TextFormat;

import java.util.List;
import java.util.Map;


public class Main extends PluginBase implements Listener{
    private TaskHandler runningTask;
    private int warnTime;
    private int repeatTime;
    private boolean deleteMobs;
    private boolean deleteAnimals;
    private boolean deleteProjectile;
    private boolean deleteExpOrb;
    private String fiveSeconds;
    private String oneSecond;
    private String cleaned;
    private String prefix;


    @Override
    public void onEnable() {
        List<String> authors = this.getDescription().getAuthors();
        this.getLogger().info(TextFormat.DARK_GREEN + "Plugin by "+authors.get(0));
        this.saveDefaultConfig();
        warnTime = this.getConfig().getInt("warnBefore", 5);
        repeatTime = ( this.getConfig().getInt("repeatTime", 300) * 20 );
        deleteMobs = this.getConfig().getBoolean("deleteMobs", false);
        deleteAnimals = this.getConfig().getBoolean("deleteAnimals", false);
        fiveSeconds = this.getConfig().getString("5+seconds");
        oneSecond = this.getConfig().getString("1second");
        cleaned = this.getConfig().getString("cleaned");
        prefix = this.getConfig().getString("prefix");
        deleteExpOrb = this.getConfig().getBoolean("deleteExpOrb");
        deleteProjectile = this.getConfig().getBoolean("deleteProjectile");
        if(warnTime > repeatTime/20){
            this.getLogger().error(TextFormat.RED + "Warn time is longer than repeat time! Change that in config.yml and use /reload.");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        runTask();
        this.getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!cmd.getName().toLowerCase().equals("ac")) return true;

        if (args.length == 0) {
            sender.sendMessage(TextFormat.GREEN + "-- autocleaner " + this.getDescription().getVersion() + " --");
            sender.sendMessage(TextFormat.GREEN + "/ac clean-items - Cleaning only items on the ground immediately");
            sender.sendMessage(TextFormat.GREEN + "/ac clean-all - Cleaning items on the ground(optional: mobs and animals) immediately");
            sender.sendMessage(TextFormat.GREEN + "/ac time - Shows time to next clean");
            sender.sendMessage(TextFormat.GREEN + "/ac stop - Stops cleaning");
            sender.sendMessage(TextFormat.GREEN + "/ac start - Starts cleaning");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "clean-items":
                this.getServer().broadcastMessage(prefix+TextFormat.RED + cleaned);
                cleanAll(true);
                break;
            case "clean-all":
                this.getServer().broadcastMessage(prefix+TextFormat.RED + cleaned);
                cleanAll(false);
                break;
            case "time":
                if(!runningTask.isCancelled())
                    sender.sendMessage(TextFormat.GREEN + ""+ ( (runningTask.getNextRunTick() - this.getServer().getTick())/20 + warnTime ) + " seconds left to clean.");
                else
                    sender.sendMessage(TextFormat.RED + "autocleaner is stopped!");
                break;
            case "stop":
                if(!runningTask.isCancelled()) {
                    this.getServer().getScheduler().cancelTask(runningTask.getTaskId());
                    sender.sendMessage(TextFormat.GREEN + "Cleaning stop.");
                }
                else{
                    sender.sendMessage(TextFormat.RED + "You have to start it first!");
                }
                break;
            case "start":
                if(runningTask.isCancelled()){
                    sender.sendMessage(TextFormat.GREEN + "Cleaning start.");
                    runTask();
                }
                else{
                    sender.sendMessage(TextFormat.RED + "You have to stop it first!");
                }
                break;
            default:
                sender.sendMessage(TextFormat.RED +"Unknown command, use /ac to list available commands.");
        }
        return true;
    }

    private void runTask(){
        runningTask = this.getServer().getScheduler().scheduleDelayedRepeatingTask(this, () -> {
            WarnRunnable runnable = new WarnRunnable(this, warnTime);
            runnable.setId(this.getServer().getScheduler().scheduleRepeatingTask(this, runnable,20).getTaskId());
        }, repeatTime-(warnTime*20), repeatTime);
    }

    int cleanAll(boolean onlyItems){
        int counter = 0;
        Server s = this.getServer();
        Map<Integer, Level> levels = s.getLevels();
        for(Map.Entry<Integer, Level> level : levels.entrySet()){
            level.getValue().doChunkGarbageCollection();
            level.getValue().unloadChunks(true);
            Entity[] entities = level.getValue().getEntities();
            for(Entity entity : entities){
                if(entity instanceof EntityItem){
                    level.getValue().removeEntity(entity);
                    counter++;
                }
                else if( entity instanceof EntityMob && deleteMobs && !onlyItems){
                    level.getValue().removeEntity(entity);
                    counter++;
                }
                else if( ( entity instanceof EntityAnimal || entity instanceof EntityWaterAnimal) && deleteAnimals && !onlyItems){
                    level.getValue().removeEntity(entity);
                    counter++;
                }
                else if((entity instanceof EntityProjectile) && deleteProjectile){
                    level.getValue().removeEntity(entity);
                    counter++;
                }
                else if((entity instanceof EntityXPOrb) && deleteExpOrb){
                    level.getValue().removeEntity(entity);
                    counter++;
                }
            }
        }
        return counter;
    }

    boolean getDeleteMobs(){
        return deleteMobs;
    }

    boolean getDeleteAnimals(){
        return deleteAnimals;
    }

    public String getFiveSeconds() {
        return fiveSeconds;
    }

    public String getOneSecond() {
        return oneSecond;
    }

    public String getCleaned() {
        return cleaned;
    }

    public String getPrefix() {
        return prefix;
    }
}
