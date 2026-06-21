package local.redcare.service.github;

import org.junit.jupiter.api.Test;

import static local.redcare.service.github.QuotaGate.Mode.*;
import static org.assertj.core.api.Assertions.assertThat;

public class QuotaGateTest {

    @Test
    void updateQuota_climbUp_and_down() {
        QuotaGate gate = new QuotaGate();
        assertThat(gate.getMode()).isEqualTo(FULL_OPEN);

        gate.updateQuota(751);
        assertThat(gate.getMode()).isEqualTo(HALF_OPEN);

        gate.updateQuota(901);
        assertThat(gate.getMode()).isEqualTo(DRIPPING);

        gate.updateQuota(800);
        assertThat(gate.getMode()).isEqualTo(HALF_OPEN);

        gate.updateQuota(600);
        assertThat(gate.getMode()).isEqualTo(FULL_OPEN);
    }

}
