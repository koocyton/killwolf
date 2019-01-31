package com.doopp.gauss.common.task;

import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class ScheduledTasks{

    public void reportCurrentTime(){
        System.out.println ("Scheduling Tasks Examples: The time is now " + dateFormat ().format (new Date()));
    }

    //@Scheduled(fixedRate = 1000 * 30)
    //public void reportCurrentTime(){
    //    System.out.println ("Scheduling Tasks Examples: The time is now " + dateFormat ().format (new Date()));
    //}

    //每1分钟执行一次
    //@Scheduled(cron = "0 */1 *  * * * ")
    //public void reportCurrentByCron(){
    //    System.out.println ("Scheduling Tasks Examples By Cron: The time is now " + dateFormat ().format (new Date ()));
    //}

    private SimpleDateFormat dateFormat(){
        return new SimpleDateFormat ("HH:mm:ss");
    }

}