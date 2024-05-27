    package com.example.ultimateone.Model;

    // DHT11Data.java
    public class DHT11Data {
        private int humidite;
        private int humiditesoil;
        private double temperature;

        // Default constructor required for calls to DataSnapshot.getValue(DHT11Data.class)
        public DHT11Data() {
        }

        // Getters and setters
        public int getHumidite() {
            return humidite;
        }

        public void setHumidite(int humidite) {
            this.humidite = humidite;
        }

        public int getHumiditesoil() {
            return humiditesoil;
        }

        public void setHumiditesoil(int humiditesoil) {
            this.humiditesoil = humiditesoil;
        }

        public double getTemperature() {
            return temperature;
        }

        public void setTemperature(double temperature) {
            this.temperature = temperature;
        }
    }

