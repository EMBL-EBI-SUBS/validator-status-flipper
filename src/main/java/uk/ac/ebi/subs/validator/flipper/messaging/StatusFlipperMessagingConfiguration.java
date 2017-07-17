package uk.ac.ebi.subs.validator.flipper.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import uk.ac.ebi.subs.validator.messaging.Queues;
import uk.ac.ebi.subs.validator.messaging.RoutingKeys;
import uk.ac.ebi.subs.validator.messaging.ValidationExchangeConfig;

/**
 * Messaging configuration for the validator status flipper service.
 *
 * Created by karoly on 17/07/2017.
 */
@Configuration
@ComponentScan(basePackageClasses = ValidationExchangeConfig.class)
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
        return new Queue(Queues.VALIDATION_RESULT_DOCUMENT_UPDATE, true);
    }

    /**
     * Create a {@link Binding} between the validation exchange and the validation result document queue
     * using the routing key of validation result document updated.
     *
     * @param validationResultDocumentQueue {@link Queue} for validation result document events
     * @param validationExchange {@link TopicExchange} for validation
     * @return a {@link Binding} between the validation exchange and the validation result document queue
     * using the routing key of validation result document updated.
     */
    @Bean
    Binding validationResultDocumentUpdatedBinding(Queue validationResultDocumentQueue, TopicExchange validationExchange) {
        return BindingBuilder.bind(validationResultDocumentQueue).to(validationExchange)
                .with(RoutingKeys.EVENT_VALIDATION_RESULT_DOCUMENT_UPDATED);
    }
}
