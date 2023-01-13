package com.example.marasigan.worksampler.views;

import com.example.marasigan.worksampler.entities.Sample;
import com.example.marasigan.worksampler.entities.StudyObject;


public class SchedData {
    private StudyObject studyObject;
    private Sample sample;

    public StudyObject getStudyObject() {
        return studyObject;
    }

    public void setStudyObject(StudyObject studyObject) {
        this.studyObject = studyObject;
    }

    public Sample getSample() {
        return sample;
    }

    public void setSample(Sample sample) {
        this.sample = sample;
    }

}