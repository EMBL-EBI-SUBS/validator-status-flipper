package uk.ac.ebi.subs.validator.flipper.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import uk.ac.ebi.subs.messaging.ExchangeConfig;
import uk.ac.ebi.subs.messaging.Queues;

import static uk.ac.ebi.subs.validator.flipper.messaging.StatusFlipperQueues.VALIDATION_RESULT_DOCUMENT_UPDATE;
import static uk.ac.ebi.subs.validator.flipper.messaging.StatusFlipperRoutingKeys.EVENT_VALIDATION_RESULT_DOCUMENT_UPDATED;

/**
 * Messaging configuration for the validator status flipper service.
 *
 * Created by karoly on 17/07/2017.
 */
@Configuration
@ComponentScan(basePackageClasses = ExchangeConfig.class)
public class StatusFlipperMessagingConfiguration {

    /**
     * Instantiate a JSON message converter.
     *
     * @return an instance of JSON message converter.
     */
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Instantiate a {@link Queue} for publish events related to the validation result document.
     *
     * @return an instance of a {@link Queue} for publish events related to the validation result document.
     */
    @Bean
    Queue validationResultDocumentQueue() {
        return Queues.buildQueueWithDlx(VALIDATION_RESULT_DOCUMENT_UPDATE);
    }

    /**
     * Create a {@link Binding} between the submission exchange and the validation result document queue
     * using the routing key of validation result document updated.
     *
     * @param validationResultDocumentQueue {@link Queue} for validation result document events
     * @param submissionExchange {@link TopicExchange} for validation
     * @return a {@link Binding} between the validation exchange and the validation result document queue
     * using the routing key of validation result document updated.
     */
    @Bean
    Binding validationResultDocumentUpdatedBinding(Queue validationResultDocumentQueue, TopicExchange submissionExchange) {
        return BindingBuilder.bind(validationResultDocumentQueue).to(submissionExchange)
                .with(EVENT_VALIDATION_RESULT_DOCUMENT_UPDATED);
    }
}
