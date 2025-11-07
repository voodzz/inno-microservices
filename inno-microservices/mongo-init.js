db = db.getSiblingDB('paymentservice');

db.createUser({
    user: "myuser",
    pwd: "secret",
    roles: [
        { role: "readWrite", db: "paymentservice" }
    ]
});