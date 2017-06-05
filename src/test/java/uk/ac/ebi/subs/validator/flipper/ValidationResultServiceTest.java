package uk.ac.ebi.subs.validator.flipper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.subs.data.component.Archive;
import uk.ac.ebi.subs.validator.data.ValidationResult;
import uk.ac.ebi.subs.validator.data.ValidationStatus;
import uk.ac.ebi.subs.validator.repository.ValidationResultRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Created by karoly on 18/05/2017.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@EnableMongoRepositories(basePackageClasses = ValidationResultRepository.class)
@EnableAutoConfiguration
@SpringBootTest(classes = ValidationResultService.class)
public class ValidationResultServiceTest {

    @Autowired
    ValidationResultRepository repository;

    @Autowired
    ValidationResultService service;

    private ValidationResult existingValidationResult;

    @Before
    public void setUp() {
        repository.deleteAll();
        existingValidationResult = createValidationResult(getInitialExpectedResults(), 3, "123", "456");
        repository.insert(existingValidationResult);
    }

    @Test
    public void updatingNotExistingValidationResultDocumentReturnFalse() {
        repository.delete(existingValidationResult);

        assertThat(service.updateValidationResult(existingValidationResult.getUuid()), is(false));
    }

    @Test
    public void notAllEntityHasBeenValidatedShouldLeaveValidationStatusPending() {
        assertThat(existingValidationResult.getValidationStatus() == ValidationStatus.Pending, is(true));

        existingValidationResult.getExpectedResults().put(Archive.ArrayExpress, true);
        repository.save(existingValidationResult);

        service.updateValidationResult(existingValidationResult.getUuid());

        ValidationResult actualValidationResultDocument = repository.findOne(existingValidationResult.getUuid());

        assertThat(actualValidationResultDocument.getValidationStatus() == ValidationStatus.Pending, is(true));
        assertThat(actualValidationResultDocument.getExpectedResults().get(Archive.ArrayExpress), is(true));
        assertThat(actualValidationResultDocument.getExpectedResults().get(Archive.BioSamples), is(false));
        assertThat(actualValidationResultDocument.getExpectedResults().get(Archive.Ena), is(false));

    }

    @Test
    public void allEntityHasBeenValidatedShouldChangeValidationStatusToComplete() {
        assertThat(existingValidationResult.getValidationStatus() == ValidationStatus.Pending, is(true));

        existingValidationResult.getExpectedResults().put(Archive.ArrayExpress, true);
        existingValidationResult.getExpectedResults().put(Archive.BioSamples, true);
        existingValidationResult.getExpectedResults().put(Archive.Ena, true);
        repository.save(existingValidationResult);

        service.updateValidationResult(existingValidationResult.getUuid());

        ValidationResult actualValidationOutcomeDocument = repository.findOne(existingValidationResult.getUuid());

        assertThat(actualValidationOutcomeDocument.getValidationStatus() == ValidationStatus.Complete, is(true));
        assertThat(actualValidationOutcomeDocument.getExpectedResults().get(Archive.ArrayExpress), is(true));
        assertThat(actualValidationOutcomeDocument.getExpectedResults().get(Archive.BioSamples), is(true));
        assertThat(actualValidationOutcomeDocument.getExpectedResults().get(Archive.Ena), is(true));

    }

    private ValidationResult createValidationResult(Map<Archive, Boolean> expectedResults, int version, String submissionId, String entityUuid) {
        ValidationResult validationResult = new ValidationResult();
        validationResult.setUuid(UUID.randomUUID().toString());
        validationResult.setExpectedResults(expectedResults);
        validationResult.setVersion(version);
        validationResult.setSubmissionId(submissionId);
        validationResult.setEntityUuid(entityUuid);

        return validationResult;
    }

    private Map<Archive, Boolean> getInitialExpectedResults() {
        Map<Archive, Boolean> expectedResults = new HashMap<>();
        expectedResults.put(Archive.ArrayExpress, false);
        expectedResults.put(Archive.BioSamples, false);
        expectedResults.put(Archive.Ena, false);
        return expectedResults;
    }
}
