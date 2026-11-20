# Edit Profile — spec

The signed-in user views and changes their profile: photo, full name, date of birth, email, and phone number.

**Firestore document:** `users/{uid}`.
**Out of scope:** deleting the account, changing the password, and changing the email of accounts that sign in only with Google or Facebook.

Requirement IDs are stable. Tests and reviews cite them (for example "FR-012").

## Edit Profile screen

- **FR-001** The screen loads the user's photo, full name, date of birth, email, and phone number from the profile, and shows a loading state until they arrive.
- **FR-002** The back arrow returns to the Profile tab without saving.
- **FR-003** Tapping the email row opens Change Email. Tapping the phone row opens Change Phone.
- **FR-004** The email row shows a "Verified" badge only when Firebase Auth reports the email as verified.
- **FR-005** The phone row shows a "Verified" badge only when the number is linked to the Firebase Auth account. With no phone number, the row shows "Not added" and an Add button instead of Change.

## Photo

- **FR-010** The avatar shows, in this order of priority: the photo just picked, the saved custom photo, the Google account photo, the user's initials, and finally a person icon.
- **FR-011** Picking a photo only previews it. Nothing is saved until the user taps Save.
- **FR-012** When the user saves with a picked photo, the photo is scaled so its longest side is at most 256 px, compressed as JPEG at quality 80, and stored as Base64 in the `photoBase64` field. `photoSource` is set to `"custom"`.
- **FR-013** If the encoded photo is larger than 200 KB, nothing is saved and the user sees an error asking for a smaller image.
- **FR-014** Saving without a picked photo does not change `photoBase64`, `photoUrl`, or `photoSource`.
- **FR-015** Google sign-in sync never overwrites a custom photo or a custom name (`photoSource` / `nameSource` equal to `"custom"`).

## Name and date of birth

- **FR-020** Full name is required. It is trimmed, must not be blank, and is at most 50 characters. Saving a changed name sets `nameSource` to `"custom"`.
- **FR-021** The date of birth is chosen with a date picker (tapping the field or its calendar icon). It cannot be in the future and is stored as an ISO date (`yyyy-MM-dd`).
- **FR-022** Save writes all changed fields in a single Firestore write and sets `updatedAt` to the server timestamp. Unchanged fields are not written.
- **FR-023** While saving, the Save button shows progress and ignores further taps.
- **FR-024** On success, a "Profile updated" snackbar appears. On failure, an error snackbar appears and the entered values stay on screen.

## Change Email

Only for accounts that have an email/password sign-in.

- **FR-030** The user enters the new email and their current password.
- **FR-031** The new email must be a valid address and differ from the current one. Otherwise an inline error shows and nothing is sent.
- **FR-032** The app re-authenticates with the current password before requesting the change. A wrong password shows an error, and no email is sent.
- **FR-033** After re-authentication, the app calls Firebase `verifyBeforeUpdateEmail(newEmail)` and shows a "Check your inbox" state naming the new address. The sign-in email changes only after the user clicks the link.
- **FR-034** Once Firebase Auth reports the new email, the `email` field in `users/{uid}` is updated to match the next time the profile loads.
- **FR-035** Errors map to specific messages: email already in use, too many requests, network failure.
- **FR-036** For accounts that sign in only with Google or Facebook, the screen explains that the email is managed by that provider and offers no change.
- **FR-037** While a change is waiting for its link, Edit Profile shows the new address under Email with Resend and Cancel. Change Email opens on "Check your inbox" for it.
- **FR-038** "Check your inbox" offers Open Email App, Use a different email, and Resend link after a 30-second wait. When the user comes back to the app, it checks whether the link was clicked and shows "Email updated". If the change ended the session, it asks the user to sign in again with the new email.

> Delivery note: on the Spark plan, Firebase drops auth emails until a custom SMTP sender is configured.

## Change Phone

- **FR-040** The user picks a country code and enters a number, using the existing phone input from the auth screens.
- **FR-041** The app sends an SMS code through the existing `sendOTPCode` flow and opens the code-entry screen.
- **FR-042** A correct code updates the phone credential on the Firebase Auth account and saves the number (E.164 format) to `users/{uid}.phone`.
- **FR-043** A wrong or expired code shows an error. The user can request a new code after the same cooldown as the existing OTP screen.
- **FR-044** A number already linked to another account shows an error, and nothing is saved.

> Development note: use Firebase test phone numbers. The Spark plan has a small daily SMS quota.

## Non-functional

- **NFR-001** No photo data, email address, phone number, password, or verification code is written to logs.
- **NFR-002** All user-visible text comes from `strings.xml`.

## Tests (core logic only)

- **T-001** Photo save rules: FR-012, FR-013, FR-014.
- **T-002** Name and date validation: FR-020, FR-021.
- **T-003** Only changed fields are written: FR-022.
- **T-004** Change Email ViewModel: wrong password, same email, success, confirmed after the link (FR-031 to FR-033, FR-038); Firebase error mapping (FR-035).
- **T-005** Change Phone ViewModel: correct code, wrong code, number in use (FR-042 to FR-044).
