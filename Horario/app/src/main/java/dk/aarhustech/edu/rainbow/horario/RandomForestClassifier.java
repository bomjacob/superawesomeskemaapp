package dk.aarhustech.edu.rainbow.horario;

import com.google.gson.Gson;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"MismatchedReadAndWriteOfArray", "unused", "MismatchedQueryAndUpdateOfCollection"})
class RandomForestClassifier {
    private Data data;
    private int nClasses;
    private int nEstimators;

    RandomForestClassifier(InputStream stream) throws UnsupportedEncodingException {
        Reader reader = new InputStreamReader(stream, "UTF-8");
        Gson gson = new Gson();
        this.data = gson.fromJson(reader, Data.class);
        this.nEstimators = this.data.forest.size();
        this.nClasses = this.data.forest.get(0).classes[0].length;
    }

    private static int findMax(double[] nums) {
        int index = 0;
        for (int i = 0; i < nums.length; i++) {
            index = nums[i] > nums[index] ? i : index;
        }
        return index;
    }

    double getMaxScore() {
        return (double) nEstimators;
    }

    Map<String, Double> predict_proba(Map<String, Double> features) {
        double[] featureArray = new double[this.data.features.length];
        for (int i = 0; i < this.data.features.length; i++) {
            featureArray[i] = features.containsKey(this.data.features[i]) ? features.get(this.data.features[i]) : 0.0;
        }
        return predict_proba(featureArray);
    }

    Map<String, Double> predict_proba(double[] features) {
        double[] classes = new double[this.nClasses];
        for (int i = 0; i < this.nEstimators; i++) {
            classes[this.data.forest.get(i).predict(features, 0)]++;
        }
        Map<String, Double> out = new HashMap<>();
        for (int i = 0; i < this.data.classes.length; i++) {
            out.put(this.data.classes[i], classes[i]);
        }
        return out;
    }

    public String predict(double[] features) {
        double[] classes = new double[this.nClasses];
        for (int i = 0; i < this.nEstimators; i++) {
            classes[this.data.forest.get(i).predict(features, 0)]++;
        }
        return this.data.classes[RandomForestClassifier.findMax(classes)];
    }

    private class Data {
        private String[] features;
        private String[] classes;
        private List<Tree> forest;

        private class Tree {
            private int[] childrenLeft;
            private int[] childrenRight;
            private double[] thresholds;
            private int[] indices;
            private double[][] classes;

            private int predict(double[] features, int node) {
                if (this.thresholds[node] != -2) {
                    if (features[this.indices[node]] <= this.thresholds[node]) {
                        return this.predict(features, this.childrenLeft[node]);
                    } else {
                        return this.predict(features, this.childrenRight[node]);
                    }
                }
                return RandomForestClassifier.findMax(this.classes[node]);
            }

            private int predict(double[] features) {
                return this.predict(features, 0);
            }
        }
    }
}