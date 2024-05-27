package com.example.ultimateone.Model;

public class DataResponse {
    private DHT11Data DHT11;
    private String pompeRun;
    private String vontilateurRun;
    private Predictions predictions;

    // Default constructor required for calls to DataSnapshot.getValue(DataResponse.class)
    public DataResponse() {
    }

    // Getters and setters
    public DHT11Data getDHT11() {
        return DHT11;
    }

    public void setDHT11(DHT11Data DHT11) {
        this.DHT11 = DHT11;
    }

    public String getPompeRun() {
        return pompeRun;
    }

    public void setPompeRun(String pompeRun) {
        this.pompeRun = pompeRun;
    }

    public String getVontilateurRun() {
        return vontilateurRun;
    }

    public void setVontilateurRun(String vontilateurRun) {
        this.vontilateurRun = vontilateurRun;
    }

    public Predictions getPredictions() {
        return predictions;
    }

    public void setPredictions(Predictions predictions) {
        this.predictions = predictions;
    }

    // Predictions inner class
    public static class Predictions {
        private String image;
        private String result;

        public Predictions() {
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }
    }
}
