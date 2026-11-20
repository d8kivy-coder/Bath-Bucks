const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validProfilePayload(uid, timestamp) {
  return {
    userId: uid,
    displayName: "Ducky Player",
    balanceCents: 100000,
    wagerCents: 100,
    progressiveCents: 253759,
    totalSpins: 0,
    totalWonCents: 0,
    biggestWinCents: 0,
    soundEnabled: true,
    hapticsEnabled: true,
    skillModeEnabled: true,
    createdAt: timestamp,
    updatedAt: timestamp,
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: denied read/write on profiles and winHistory", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const ts = new Date(Date.now() - 1000);

  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID, ts))
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("winHistory").get()
  );
});

test("Authenticated owner: valid Kotlin profile create, partial updates, and query succeed", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 10000);

  // Mirror FirestoreRepository.ensureProfileExists create payload (NO @DocumentId `id`)
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID, created))
  );

  // Owner get succeeds; listing /users is forbidden
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());
  await assertFails(aliceDb.collection("users").get());

  // Mirror FirestoreRepository.saveGameplayStats partial update
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      displayName: "Ducky Champion",
      balanceCents: 102500,
      wagerCents: 200,
      progressiveCents: 253765,
      totalSpins: 1,
      totalWonCents: 2600,
      biggestWinCents: 2600,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Mirror FirestoreRepository.saveSettings partial update
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      soundEnabled: false,
      hapticsEnabled: true,
      skillModeEnabled: false,
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("Profile security invariants: reject ghost fields, immutable edits, invalid bounds, and future timestamps", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 10000);

  // Reject @DocumentId `id` or ghost field on create
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set({
      ...validProfilePayload(ALICE_UID, created),
      id: ALICE_UID,
    })
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set({
      ...validProfilePayload(ALICE_UID, created),
      isAdmin: true,
    })
  );

  // Reject invalid wager tier or future timestamp on create
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set({
      ...validProfilePayload(ALICE_UID, created),
      wagerCents: 999,
    })
  );
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .set(validProfilePayload(ALICE_UID, new Date(Date.now() + 60000)))
  );

  // Seed valid profile
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID, created))
  );

  // Reject mutating immutable createdAt or userId
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      createdAt: new Date(Date.now() - 2000),
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      userId: BOB_UID,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject value poisoning (negative balance / oversized displayName)
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      balanceCents: -50,
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      displayName: "x".repeat(65),
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("Cross-user isolation: Alice cannot read, update, or delete Bob's profile or winHistory", async () => {
  const created = new Date(Date.now() - 10000);
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const adminDb = context.firestore();
    await adminDb.collection("users").doc(BOB_UID).set(validProfilePayload(BOB_UID, created));
    await adminDb
      .collection("users")
      .doc(BOB_UID)
      .collection("winHistory")
      .doc("win_1")
      .set({
        userId: BOB_UID,
        label: "DUCKY FREE SPINS",
        payoutCents: 4200,
        wagerCents: 100,
        category: "FREE_SPINS",
        createdAt: created,
      });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).get());
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).update({
      balanceCents: 0,
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertFails(aliceDb.collection("users").doc(BOB_UID).delete());
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).collection("winHistory").get()
  );
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).collection("winHistory").doc("win_1").get()
  );
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).collection("winHistory").doc("win_1").delete()
  );
});

test("WinHistory subcollection: Kotlin create/query/delete succeed; update and invalid category fail", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 5000);
  const historyCol = aliceDb.collection("users").doc(ALICE_UID).collection("winHistory");

  // Empty subcollection list query (matches FirestoreRepository.observeWinHistory)
  await assertSucceeds(historyCol.orderBy("createdAt", "desc").limit(20).get());

  // Mirror FirestoreRepository.recordWinHistory create payload
  await assertSucceeds(
    historyCol.doc("win_1").set({
      userId: ALICE_UID,
      label: "SHAMPOO PICK BONUS",
      payoutCents: 8400,
      wagerCents: 200,
      category: "SHAMPOO_BONUS",
      createdAt: created,
    })
  );

  // Ordered query with data succeeds
  await assertSucceeds(historyCol.orderBy("createdAt", "desc").limit(20).get());

  // WinHistory entries are immutable (update denied)
  await assertFails(historyCol.doc("win_1").update({ payoutCents: 999999 }));

  // Invalid category or ghost key denied
  await assertFails(
    historyCol.doc("win_2").set({
      userId: ALICE_UID,
      label: "INVALID",
      payoutCents: 500,
      wagerCents: 100,
      category: "UNKNOWN_TYPE",
      createdAt: created,
    })
  );
  await assertFails(
    historyCol.doc("win_3").set({
      id: "win_3",
      userId: ALICE_UID,
      label: "EXTRA ID",
      payoutCents: 500,
      wagerCents: 100,
      category: "BIG_WIN",
      createdAt: created,
    })
  );

  // Owner delete succeeds (session reset)
  await assertSucceeds(historyCol.doc("win_1").delete());
});
