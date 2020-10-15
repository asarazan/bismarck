const bismarck = require("./build/js/packages/bismarck-parent-bismarck");

async function sleep(ms) {
    return new Promise((resolve) => {
        setTimeout(resolve, ms);
    })
}

async function main() {

    const test = new bismarck.DedupingBismarck();
    const state = test.peekState();
    const serializer = bismarck.jsonSerializer();
    const bytes = serializer.serialize({ foo: "bar" });
    const obj = serializer.deserialize(bytes);

    console.log("Foo");
}

main().then();