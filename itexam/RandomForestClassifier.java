import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


class RandomForestClassifier {
    private Data data;
    private int nClasses;
    private int nEstimators;

    public RandomForestClassifier(File file) throws FileNotFoundException {
        String jsonStr = new Scanner(file).useDelimiter("\\Z").next();
        Gson gson = new Gson();
        this.data = gson.fromJson(jsonStr, Data.class);
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

    public static void main(String[] args) throws IOException {
        if (args.length > 0 && args[0].endsWith(".json")) {

            Map<String, Double> features = new HashMap<>();
            features.put("10:62:eb:97:f8:4c", -47.0);
            features.put("10:62:eb:97:f8:4e", -55.0);
            features.put("12:7b:ef:24:33:3c", -7.0);
            features.put("2e:19:60:c9:0c:9b", -79.0);
            features.put("7c:03:4c:c7:0c:22", -87.0);
            features.put("fc:3f:db:64:4d:04", -70.0);

            // Estimator:
            RandomForestClassifier clf = new RandomForestClassifier(new File(args[0]));

            // Prediction:
            Map<String, Double> prediction = clf.predict_proba(features);
            Gson gson = new Gson();
            System.out.println(gson.toJson(prediction));

        }
    }

    public Map<String, Double> predict_proba(Map<String, Double> features) {
        double[] featureArray = new double[this.data.features.length];
        for (int i = 0; i < this.data.features.length; i++) {
            featureArray[i] = features.getOrDefault(this.data.features[i], 0.0);
        }
        return predict_proba(featureArray);
    }

    public Map<String, Double> predict_proba(double[] features) {
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

    private class Data {
        private String[] features;
        private String[] classes;
        private List<Tree> forest;
    }
}