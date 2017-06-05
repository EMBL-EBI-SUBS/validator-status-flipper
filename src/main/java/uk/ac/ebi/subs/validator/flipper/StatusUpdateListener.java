package uk.ac.ebi.subs.validator.flipper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.validator.messaging.Queues;

/**
 * This class is listening on events on the validation aggregation results {@code Queue}.
 * When processing a published event it will update the {@code ValidationResult} document's status
 * according to the availability of the validation results. If all the entity has been validated,
 * then the status will change to {@code Complete}, otherwise it will stay {@code Pending} as initially.
 */
@Service
public class StatusUpdateListener {
    public static final Logger logger = LoggerFactory.getLogger(StatusUpdateListener.class);

    private RabbitMessagingTemplate rabbitMessagingTemplate;

    @Autowired
    private ValidationResultService validationResultService;

    @Autowired
    public StatusUpdateListener(RabbitMessagingTemplate rabbitMessagingTemplate) {
        this.rabbitMessagingTemplate = rabbitMessagingTemplate;
    }

    @RabbitListener(queues = Queues.VALIDATION_RESULT_DOCUMENT_UPDATE)
    public void processUpdate(String uuid) {
        logger.debug("Processing validation result document update with id {}.", uuid);

        if (!validationResultService.updateValidationResult(uuid)) {
            logger.debug("Ignoring obsolete validation documents.");
        }
    }
}