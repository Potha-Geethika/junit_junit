package com.carbo.pad.events.source;

import com.carbo.pad.events.model.PadChangeModel;
import com.carbo.pad.model.Pad;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class PadTimezoneSourceBean {
    private static final Logger logger = LoggerFactory.getLogger(PadTimezoneSourceBean.class);

    private final StreamBridge streamBridge;

    @Autowired
    public PadTimezoneSourceBean(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    public void publishPadTimezoneChange(String action, Pad updatedPad, String previousPadTimezone) {
        logger.debug("Sending Kafka message {} for Organization Id: {}", action, updatedPad.getOrganizationId());
        PadChangeModel change = new PadChangeModel(
                PadChangeModel.class.getTypeName(),
                action,
                updatedPad,
                previousPadTimezone
        );
        streamBridge.send("padTimezoneChange-out-0", MessageBuilder.withPayload(change).build());
    }
}