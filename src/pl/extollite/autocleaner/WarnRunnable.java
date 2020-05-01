package pl.extollite.autocleaner;

import cn.nukkit.utils.TextFormat;

public class WarnRunnable implements Runnable {
    private int id;
    private int counter;
    private Main plugin;

    WarnRunnable(Main plugin, int counter){
        this.counter = counter;
        this.plugin = plugin;
    }

    public int getId() {
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    @Override
    public void run(){
        if(counter == 0){
            plugin.getServer().broadcastMessage(plugin.getPrefix()+TextFormat.RED + plugin.getCleaned().replace("%counter%", String.valueOf(plugin.cleanAll(false))));
            plugin.getServer().getScheduler().cancelTask(id);
        }
        else if(counter == 1){
            plugin.getServer().broadcastMessage(plugin.getPrefix()+TextFormat.RED + plugin.getOneSecond().replace("%counter%", String.valueOf(counter)) );
            counter--;
        }
        else{
            plugin.getServer().broadcastMessage(plugin.getPrefix()+TextFormat.RED + plugin.getFiveSeconds().replace("%counter%", String.valueOf(counter)) );
            counter--;
        }
    }
}
