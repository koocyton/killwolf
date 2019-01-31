package com.doopp.gauss.server.configuration;

import com.doopp.gauss.common.task.OnlineCheckTask;
import com.doopp.gauss.common.task.ScheduledTasks;
import com.doopp.gauss.common.utils.IdWorker;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.text.ParseException;

public class CommonConfiguration {

    @Bean
    public ApplicationProperties applicationProperties() {
       return new ApplicationProperties();
    }

    @Bean
    public IdWorker userIdWorker() {
        return new IdWorker(1, 1);
    }

    @Bean
    public ThreadPoolTaskExecutor gameTaskExecutor () {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(5);
        threadPoolTaskExecutor.setMaxPoolSize(1000);
        threadPoolTaskExecutor.setQueueCapacity(25);
        threadPoolTaskExecutor.setKeepAliveSeconds(30000);
        return threadPoolTaskExecutor;
    }


    @Bean
    public ScheduledTasks scheduledTasks() {
        return new ScheduledTasks();
    }

    @Bean
    public MethodInvokingJobDetailFactoryBean methodInvokingJobDetail (ScheduledTasks scheduledTasks) {
        MethodInvokingJobDetailFactoryBean methodInvokingJobDetailFactoryBean = new MethodInvokingJobDetailFactoryBean();
        methodInvokingJobDetailFactoryBean.setTargetObject(scheduledTasks);
        methodInvokingJobDetailFactoryBean.setTargetMethod("reportCurrentTime");
        return methodInvokingJobDetailFactoryBean;
    }

    @Bean
    public CronTriggerFactoryBean cronTrigger (JobDetail methodInvokingJobDetail) {
        CronTriggerFactoryBean cronTriggerFactoryBean = new CronTriggerFactoryBean();
        cronTriggerFactoryBean.setJobDetail(methodInvokingJobDetail);
        cronTriggerFactoryBean.setCronExpression("0 1,2,3,4,5,6,7 15 * * ? ");
        return cronTriggerFactoryBean;
    }

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(CronTrigger cronTrigger){
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean ();
        schedulerFactoryBean.setTriggers(cronTrigger);
        return schedulerFactoryBean;
    }
}
