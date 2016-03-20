package com.google.android.gms.samples.vision.face.facetracker;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Created by kolipass on 20.03.16.
 */
public class GraphicFaceTrackerFactoryAggregator implements MultiProcessor.Factory<Face> {
    private Collection<MultiProcessor.Factory<Face>> factories = new LinkedList<>();

    public GraphicFaceTrackerFactoryAggregator put(MultiProcessor.Factory<Face> faceTracker) {
        factories.add(faceTracker);
        return this;
    }

    @Override
    public Tracker<Face> create(Face face) {
        FaceTrackerAggregator aggregator = new FaceTrackerAggregator();
        for (MultiProcessor.Factory<Face> factory : factories) {
            aggregator.put(factory.create(face));
        }

        return aggregator;
    }

    private class FaceTrackerAggregator extends Tracker<Face> {
        private Collection<Tracker<Face>> trackers = new LinkedList<>();

        public FaceTrackerAggregator put(Tracker<Face> faceTracker) {
            trackers.add(faceTracker);
            return this;
        }

        @Override
        public void onDone() {
            for (Tracker<Face> tracker : trackers) tracker.onDone();
        }

        @Override
        public void onMissing(Detector.Detections<Face> detections) {
            for (Tracker<Face> tracker : trackers) tracker.onMissing(detections);
        }

        @Override
        public void onNewItem(int id, Face item) {
            for (Tracker<Face> tracker : trackers) tracker.onNewItem(id, item);
        }

        @Override
        public void onUpdate(Detector.Detections<Face> detections, Face item) {
            for (Tracker<Face> tracker : trackers) tracker.onUpdate(detections, item);
        }
    }
}
