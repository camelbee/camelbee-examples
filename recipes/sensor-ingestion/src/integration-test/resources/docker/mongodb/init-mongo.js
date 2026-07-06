db.createUser(
  {
    user: "mongouser",
    pwd: "password",
    roles: [
      {
        role: "readWrite",
        db: "camelbee"
      }
    ]
  }
);
db.createCollection("sensorReadings");
