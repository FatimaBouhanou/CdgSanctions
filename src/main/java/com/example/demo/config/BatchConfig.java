package com.example.demo.config;


import com.example.demo.model.SanctionedEntity;
import com.example.demo.repository.SanctionedEntityRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

    @Bean
    public ItemReader<SanctionedEntity> reader(SanctionedEntityRepository repository) {
        return new ListItemReader<>(repository.findAll());
    }

    @Bean
    public ItemProcessor<SanctionedEntity, SanctionedEntity> processor() {
        return entity -> {
            return entity;
        };
    }

    @Bean
    public ItemWriter<SanctionedEntity> writer(SanctionedEntityRepository repository) {
        return items -> repository.saveAll(items);
    }

    @Bean
    public Step step(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                     ItemReader<SanctionedEntity> reader, ItemProcessor<SanctionedEntity, SanctionedEntity> processor,
                     ItemWriter<SanctionedEntity> writer) {
        return new StepBuilder("step", jobRepository)
                .<SanctionedEntity, SanctionedEntity>chunk(100, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }


    @Bean
    public Job importSanctionsJob(JobRepository jobRepository, Step step) {
        return new JobBuilder("importSanctionsJob", jobRepository)
                .start(step)
                .incrementer(new RunIdIncrementer())
                .build();
    }

    @Bean
    public PlatformTransactionManager transactionManager(){
        return new ResourcelessTransactionManager();
    }
}
