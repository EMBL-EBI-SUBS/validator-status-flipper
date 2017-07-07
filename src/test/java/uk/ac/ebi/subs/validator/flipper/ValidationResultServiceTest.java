package uk.ac.ebi.subs.validator.flipper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.ValidationAuthor;
import uk.ac.ebi.subs.validator.data.ValidationResult;
import uk.ac.ebi.subs.validator.data.ValidationStatus;
import uk.ac.ebi.subs.validator.repository.ValidationResultRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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

        existingValidationResult.getExpectedResults().put(ValidationAuthor.Taxonomy, Arrays.asList(new SingleValidationResult()));
        repository.save(existingValidationResult);

        service.updateValidationResult(existingValidationResult.getUuid());

        ValidationResult actualValidationResultDocument = repository.findOne(existingValidationResult.getUuid());

        assertThat(actualValidationResultDocument.getValidationStatus() == ValidationStatus.Pending, is(true));
        assertThat(actualValidationResultDocument.getExpectedResults().get(ValidationAuthor.Taxonomy).isEmpty(), is(false));
        assertThat(actualValidationResultDocument.getExpectedResults().get(ValidationAuthor.Biosamples), is(new ArrayList<>()));
        assertThat(actualValidationResultDocument.getExpectedResults().get(ValidationAuthor.Ena), is(new ArrayList<>()));

    }

    @Test
    public void allEntityHasBeenValidatedShouldChangeValidationStatusToComplete() {
        assertThat(existingValidationResult.getValidationStatus() == ValidationStatus.Pending, is(true));

        existingValidationResult.getExpectedResults().put(ValidationAuthor.Taxonomy, Arrays.asList(new SingleValidationResult()));
        existingValidationResult.getExpectedResults().put(ValidationAuthor.Biosamples, Arrays.asList(new SingleValidationResult()));
        existingValidationResult.getExpectedResults().put(ValidationAuthor.Ena, Arrays.asList(new SingleValidationResult()));
        repository.save(existingValidationResult);

        service.updateValidationResult(existingValidationResult.getUuid());

        ValidationResult actualValidationResultDocument = repository.findOne(existingValidationResult.getUuid());

        assertThat(actualValidationResultDocument.getValidationStatus() == ValidationStatus.Complete, is(true));
        assertThat(actualValidationResultDocument.getExpectedResults().get(ValidationAuthor.Taxonomy).isEmpty(), is(false));
        assertThat(actualValidationResultDocument.getExpectedResults().get(ValidationAuthor.Biosamples).isEmpty(), is(false));
        assertThat(actualValidationResultDocument.getExpectedResults().get(ValidationAuthor.Ena).isEmpty(), is(false));

    }

    private ValidationResult createValidationResult(Map<ValidationAuthor, List<SingleValidationResult>> expectedResults, int version, String submissionId, String entityUuid) {
        ValidationResult validationResult = new ValidationResult();
        validationResult.setUuid(UUID.randomUUID().toString());
        validationResult.setExpectedResults(expectedResults);
        validationResult.setVersion(version);
        validationResult.setSubmissionId(submissionId);
        validationResult.setEntityUuid(entityUuid);

        return validationResult;
    }

    private Map<ValidationAuthor, List<SingleValidationResult>> getInitialExpectedResults() {
        Map<ValidationAuthor, List<SingleValidationResult>> expectedResults = new HashMap<>();
        expectedResults.put(ValidationAuthor.Taxonomy, new ArrayList<>());
        expectedResults.put(ValidationAuthor.Biosamples, new ArrayList<>());
        expectedResults.put(ValidationAuthor.Ena, new ArrayList<>());
        return expectedResults;
    }
}
