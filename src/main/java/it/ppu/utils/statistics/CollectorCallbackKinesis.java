/**
 * "FNS" (Firnet NeuroScience)
 *
 * FNS is an event-driven Spiking Neural Network framework, oriented
 * to data-driven neural simulations.
 *
 * For contacts, description, citation to acclude, licensing, please
 * read the file enclosed with code: README
 */

package it.ppu.utils.statistics;

import com.amazonaws.services.kinesis.producer.UserRecordResult;

import com.google.common.util.concurrent.FutureCallback;

import java.io.Serializable;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CollectorCallbackKinesis implements Serializable {

    private static final long serialVersionUID = 3248671994878955077L;

    FutureCallback<UserRecordResult> myCallback = new FutureCallback<UserRecordResult>() {
        @Override
        public void onFailure(Throwable t) {
            log.error("on kinesis error", t);
        }

        @Override
        public void onSuccess(UserRecordResult result) {
        }
    };
}
