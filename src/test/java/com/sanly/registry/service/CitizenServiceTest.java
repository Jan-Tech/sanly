package com.sanly.registry.service;

import com.sanly.registry.dto.request.CitizenCreateRequest;
import com.sanly.registry.dto.request.CitizenUpdateRequest;
import com.sanly.registry.dto.request.StatusUpdateRequest;
import com.sanly.registry.dto.response.CitizenResponse;
import com.sanly.registry.dto.response.CitizenVerifyResponse;
import com.sanly.registry.dto.response.PageResponse;
import com.sanly.registry.entity.*;
import com.sanly.registry.exception.CitizenNotFoundException;
import com.sanly.registry.exception.InvalidNationalIdException;
import com.sanly.registry.exception.RegistrationLimitExceededException;
import com.sanly.registry.exception.RelatedCitizenNotFoundException;
import com.sanly.registry.repository.CitizenRepository;
import com.sanly.registry.repository.NinSequenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CitizenServiceImpl.
 * All repositories are mocked; no Spring context is loaded.
 *
 * DOB 2001-01-15 is chosen deliberately: the generated NIN "50101150010" passes
 * NationalIdValidator.isValid() (year digit d[2]=1, month d[3]=0 → 10, valid range).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CitizenService")
class CitizenServiceTest {

    // ── Test fixtures ─────────────────────────────────────────────────────────
    private static final LocalDate DOB         = LocalDate.of(2001, 1, 15);
    // Expected NIN for MALE 2001-01-15 seq=1, pre-computed:
    //   prefix=5010115, partial=5010115001, check=55%11=0 → "50101150010"
    private static final String    TEST_NIN    = "50101150010";

    @Mock  CitizenRepository     citizenRepository;
    @Mock  NinSequenceRepository ninSequenceRepository;

    @InjectMocks CitizenServiceImpl service;

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Citizen buildCitizen(String nin) {
        return Citizen.builder()
                .nationalId(nin)
                .firstName("Merdan")
                .lastName("Atayew")
                .dateOfBirth(DOB)
                .gender(Gender.MALE)
                .status(CitizenStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private CitizenCreateRequest buildCreateRequest() {
        CitizenCreateRequest req = new CitizenCreateRequest();
        req.setFirstName("Merdan");
        req.setLastName("Atayew");
        req.setDateOfBirth(DOB);
        req.setGender(Gender.MALE);
        return req;
    }

    private NinSequence seqWithLast(int last) {
        NinSequence seq = new NinSequence();
        seq.setLastSequence(last);
        return seq;
    }

    // ── register() ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("register()")
    class RegisterTests {

        @BeforeEach
        void stubSequence() {
            doNothing().when(ninSequenceRepository).insertIfNotExists(anyString());
            when(ninSequenceRepository.findByPrefixWithLock(anyString()))
                    .thenReturn(Optional.of(seqWithLast(0)));
            when(ninSequenceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        }

        @Test
        @DisplayName("returns response with auto-generated NIN")
        void register_success_returnsNin() {
            CitizenCreateRequest req = buildCreateRequest();
            Citizen saved = buildCitizen(TEST_NIN);
            when(citizenRepository.save(any(Citizen.class))).thenReturn(saved);

            CitizenResponse resp = service.register(req);

            assertThat(resp.getNationalId()).isEqualTo(TEST_NIN);
            assertThat(resp.getFirstName()).isEqualTo("Merdan");
            assertThat(resp.getStatus()).isEqualTo(CitizenStatus.ACTIVE);
            verify(citizenRepository).save(any(Citizen.class));
        }

        @Test
        @DisplayName("increments sequence to 1 for first registration of this prefix")
        void register_incrementsSequence() {
            when(citizenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            buildCreateRequest();
            service.register(buildCreateRequest());

            verify(ninSequenceRepository).save(argThat(s -> s.getLastSequence() == 1));
        }

        @Test
        @DisplayName("validates fatherId exists when provided")
        void register_withFatherId_checksExistence() {
            CitizenCreateRequest req = buildCreateRequest();
            req.setFatherId(TEST_NIN);
            when(citizenRepository.existsByNationalId(TEST_NIN)).thenReturn(true);
            when(citizenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            assertThatNoException().isThrownBy(() -> service.register(req));
            verify(citizenRepository).existsByNationalId(TEST_NIN);
        }

        @Test
        @DisplayName("throws RelatedCitizenNotFoundException when fatherId does not exist")
        void register_missingFatherId_throws() {
            CitizenCreateRequest req = buildCreateRequest();
            req.setFatherId("99999999999");
            when(citizenRepository.existsByNationalId("99999999999")).thenReturn(false);

            assertThatThrownBy(() -> service.register(req))
                    .isInstanceOf(RelatedCitizenNotFoundException.class);
        }

        @Test
        @DisplayName("throws RegistrationLimitExceededException when sequence reaches 999")
        void register_sequenceExhausted_throws() {
            when(ninSequenceRepository.findByPrefixWithLock(anyString()))
                    .thenReturn(Optional.of(seqWithLast(999)));

            assertThatThrownBy(() -> service.register(buildCreateRequest()))
                    .isInstanceOf(RegistrationLimitExceededException.class);
        }
    }

    // ── getByNationalId() ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("getByNationalId()")
    class GetByNationalIdTests {

        @Test
        @DisplayName("returns full response for known NIN")
        void getByNationalId_found() {
            when(citizenRepository.findById(TEST_NIN))
                    .thenReturn(Optional.of(buildCitizen(TEST_NIN)));

            CitizenResponse resp = service.getByNationalId(TEST_NIN);

            assertThat(resp.getNationalId()).isEqualTo(TEST_NIN);
            assertThat(resp.getFirstName()).isEqualTo("Merdan");
        }

        @Test
        @DisplayName("throws CitizenNotFoundException for structurally valid but absent NIN")
        void getByNationalId_notFound_throws() {
            when(citizenRepository.findById(TEST_NIN)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getByNationalId(TEST_NIN))
                    .isInstanceOf(CitizenNotFoundException.class);
        }

        @Test
        @DisplayName("throws InvalidNationalIdException for malformed NIN")
        void getByNationalId_invalidNin_throws() {
            assertThatThrownBy(() -> service.getByNationalId("not-a-nin"))
                    .isInstanceOf(InvalidNationalIdException.class);
        }
    }

    // ── verify() ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("verify()")
    class VerifyTests {

        @Test
        @DisplayName("returns exists=true active=true for ACTIVE citizen")
        void verify_active() {
            when(citizenRepository.findById(TEST_NIN))
                    .thenReturn(Optional.of(buildCitizen(TEST_NIN)));

            CitizenVerifyResponse resp = service.verify(TEST_NIN);

            assertThat(resp.isExists()).isTrue();
            assertThat(resp.isActive()).isTrue();
        }

        @Test
        @DisplayName("returns exists=true active=false for DECEASED citizen")
        void verify_deceased() {
            Citizen deceased = buildCitizen(TEST_NIN);
            deceased.setStatus(CitizenStatus.DECEASED);
            when(citizenRepository.findById(TEST_NIN)).thenReturn(Optional.of(deceased));

            CitizenVerifyResponse resp = service.verify(TEST_NIN);

            assertThat(resp.isExists()).isTrue();
            assertThat(resp.isActive()).isFalse();
        }

        @Test
        @DisplayName("returns exists=false for unknown NIN (never throws 404)")
        void verify_notFound_neverThrows() {
            when(citizenRepository.findById(TEST_NIN)).thenReturn(Optional.empty());

            CitizenVerifyResponse resp = service.verify(TEST_NIN);

            assertThat(resp.isExists()).isFalse();
            assertThat(resp.isActive()).isFalse();
        }

        @Test
        @DisplayName("returns exists=false for structurally invalid NIN (never throws)")
        void verify_invalidNin_neverThrows() {
            CitizenVerifyResponse resp = service.verify("garbage");

            assertThat(resp.isExists()).isFalse();
            assertThatNoException();
            verifyNoInteractions(citizenRepository);
        }
    }

    // ── updateStatus() ────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateStatus() changes status field and returns updated response")
    void updateStatus_changesStatus() {
        Citizen citizen = buildCitizen(TEST_NIN);
        when(citizenRepository.findById(TEST_NIN)).thenReturn(Optional.of(citizen));
        when(citizenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        StatusUpdateRequest req = new StatusUpdateRequest();
        req.setStatus(CitizenStatus.DECEASED);

        CitizenResponse resp = service.updateStatus(TEST_NIN, req);

        assertThat(resp.getStatus()).isEqualTo(CitizenStatus.DECEASED);
    }

    // ── update() ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("update() applies non-null field changes")
    void update_appliesChanges() {
        Citizen citizen = buildCitizen(TEST_NIN);
        when(citizenRepository.findById(TEST_NIN)).thenReturn(Optional.of(citizen));
        when(citizenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CitizenUpdateRequest req = new CitizenUpdateRequest();
        req.setFirstName("Gurban");
        req.setLastName("Gurbanov");

        CitizenResponse resp = service.update(TEST_NIN, req);

        assertThat(resp.getFirstName()).isEqualTo("Gurban");
        assertThat(resp.getLastName()).isEqualTo("Gurbanov");
    }

    // ── updatePhone() ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("updatePhone() stores phone and returns masked value")
    void updatePhone_masksPhone() {
        Citizen citizen = buildCitizen(TEST_NIN);
        citizen.setPhoneNumber("+99361234567");
        when(citizenRepository.findById(TEST_NIN)).thenReturn(Optional.of(citizen));
        when(citizenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CitizenResponse resp = service.updatePhone(TEST_NIN, "+99361234567");

        assertThat(resp.getPhoneMasked()).isEqualTo("***4567");
    }

    // ── search() ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("search() maps repository page to PageResponse")
    void search_returnsPageResponse() {
        Citizen citizen = buildCitizen(TEST_NIN);
        when(citizenRepository.search(any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(citizen)));

        PageResponse<CitizenResponse> page = service.search("Merdan", null, null, 0, 20);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getNationalId()).isEqualTo(TEST_NIN);
        assertThat(page.getTotalElements()).isEqualTo(1);
    }
}
