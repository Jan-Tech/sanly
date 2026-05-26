package com.sanly.civil.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "certificate_sequences")
@Getter
@Setter
public class CertificateSequence {

    @EmbeddedId
    private CertificateSequenceId id;

    @Column(nullable = false)
    private long lastValue;

    @Embeddable
    @Getter
    @Setter
    public static class CertificateSequenceId implements Serializable {
        private String seqType;
        private int year;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CertificateSequenceId that)) return false;
            return year == that.year && Objects.equals(seqType, that.seqType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(seqType, year);
        }
    }
}
