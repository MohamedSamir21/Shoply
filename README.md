# Shoply

Shoply is an Android shopping app: browse local stores, add products to your cart, check out with flexible payment (card, installments or cash on delivery), and follow your order from store to door.

It is built with Kotlin and Jetpack Compose on Clean Architecture (data / domain / presentation per feature) with MVI-style ViewModels, Hilt for DI, and Firebase as the backend.

## Status

| Area | State |
| --- | --- |
| Authentication: splash, onboarding, sign in / sign up, social login, account verification, password reset (email link or SMS OTP) | Built and tested |
| Shopping: home, categories, search with filters and sort, product listing, product details, stores, wishlist, cart, checkout, orders, notifications | Built |
| Account: profile, addresses, payment methods, settings (light / dark theme), help center, support chat | Built |
| Edit profile | In progress |

## Screenshots

Taken from the app running on a phone, each in light and dark theme. The UI was designed in **Claude Design**: a calm Material 3 layout with a single terracotta-orange accent and warm neutral surfaces.

<table>
  <tr>
    <td align="center"><b>Home</b><br>
      <img src="screenshots/home.png" width="180" alt="Home" />
      <img src="screenshots/home-dark.png" width="180" alt="Home, dark theme" />
    </td>
    <td align="center"><b>Search</b><br>
      <img src="screenshots/search.png" width="180" alt="Search results" />
      <img src="screenshots/search-dark.png" width="180" alt="Search results, dark theme" />
    </td>
  </tr>
  <tr>
    <td align="center"><b>Product details</b><br>
      <img src="screenshots/product.png" width="180" alt="Product details" />
      <img src="screenshots/product-dark.png" width="180" alt="Product details, dark theme" />
    </td>
    <td align="center"><b>Checkout</b><br>
      <img src="screenshots/checkout.png" width="180" alt="Checkout, payment step" />
      <img src="screenshots/checkout-dark.png" width="180" alt="Checkout, payment step, dark theme" />
    </td>
  </tr>
  <tr>
    <td align="center"><b>My orders</b><br>
      <img src="screenshots/orders.png" width="180" alt="My orders" />
      <img src="screenshots/orders-dark.png" width="180" alt="My orders, dark theme" />
    </td>
    <td align="center"><b>Support chat</b><br>
      <img src="screenshots/support-chat.png" width="180" alt="Support chat" />
      <img src="screenshots/support-chat-dark.png" width="180" alt="Support chat, dark theme" />
    </td>
  </tr>
</table>

<details>
<summary><b>More screens</b></summary>

### Onboarding and sign in

<table>
  <tr>
    <td align="center"><b>Onboarding</b><br>
      <img src="screenshots/onboarding.png" width="180" alt="Onboarding" />
      <img src="screenshots/onboarding-dark.png" width="180" alt="Onboarding, dark theme" />
    </td>
    <td align="center"><b>Sign in</b><br>
      <img src="screenshots/sign-in.png" width="180" alt="Sign in" />
      <img src="screenshots/sign-in-dark.png" width="180" alt="Sign in, dark theme" />
    </td>
  </tr>
  <tr>
    <td align="center"><b>Sign up</b><br>
      <img src="screenshots/sign-up.png" width="180" alt="Sign up" />
      <img src="screenshots/sign-up-dark.png" width="180" alt="Sign up, dark theme" />
    </td>
  </tr>
</table>

### Shopping

<table>
  <tr>
    <td align="center"><b>Categories</b><br>
      <img src="screenshots/categories.png" width="180" alt="Categories" />
      <img src="screenshots/categories-dark.png" width="180" alt="Categories, dark theme" />
    </td>
    <td align="center"><b>Product listing</b><br>
      <img src="screenshots/listing.png" width="180" alt="Product listing" />
      <img src="screenshots/listing-dark.png" width="180" alt="Product listing, dark theme" />
    </td>
  </tr>
  <tr>
    <td align="center"><b>Search filters</b><br>
      <img src="screenshots/search-filters.png" width="180" alt="Search filters" />
      <img src="screenshots/search-filters-dark.png" width="180" alt="Search filters, dark theme" />
    </td>
    <td align="center"><b>Store</b><br>
      <img src="screenshots/store.png" width="180" alt="Store" />
      <img src="screenshots/store-dark.png" width="180" alt="Store, dark theme" />
    </td>
  </tr>
  <tr>
    <td align="center"><b>All stores</b><br>
      <img src="screenshots/stores.png" width="180" alt="All stores" />
      <img src="screenshots/stores-dark.png" width="180" alt="All stores, dark theme" />
    </td>
    <td align="center"><b>Wishlist</b><br>
      <img src="screenshots/wishlist.png" width="180" alt="Wishlist" />
      <img src="screenshots/wishlist-dark.png" width="180" alt="Wishlist, dark theme" />
    </td>
  </tr>
  <tr>
    <td align="center"><b>Cart</b><br>
      <img src="screenshots/cart.png" width="180" alt="Cart" />
      <img src="screenshots/cart-dark.png" width="180" alt="Cart, dark theme" />
    </td>
    <td align="center"><b>Notifications</b><br>
      <img src="screenshots/notifications.png" width="180" alt="Notifications" />
      <img src="screenshots/notifications-dark.png" width="180" alt="Notifications, dark theme" />
    </td>
  </tr>
</table>

### Account

<table>
  <tr>
    <td align="center"><b>Addresses</b><br>
      <img src="screenshots/addresses.png" width="180" alt="Addresses" />
      <img src="screenshots/addresses-dark.png" width="180" alt="Addresses, dark theme" />
    </td>
    <td align="center"><b>Payment methods</b><br>
      <img src="screenshots/payment-methods.png" width="180" alt="Payment methods" />
      <img src="screenshots/payment-methods-dark.png" width="180" alt="Payment methods, dark theme" />
    </td>
  </tr>
  <tr>
    <td align="center"><b>Settings</b><br>
      <img src="screenshots/settings.png" width="180" alt="Settings" />
      <img src="screenshots/settings-dark.png" width="180" alt="Settings, dark theme" />
    </td>
    <td align="center"><b>Favorite stores</b><br>
      <img src="screenshots/favorite-stores.png" width="180" alt="Favorite stores" />
      <img src="screenshots/favorite-stores-dark.png" width="180" alt="Favorite stores, dark theme" />
    </td>
  </tr>
  <tr>
    <td align="center"><b>Help center</b><br>
      <img src="screenshots/help-center.png" width="180" alt="Help center" />
      <img src="screenshots/help-center-dark.png" width="180" alt="Help center, dark theme" />
    </td>
  </tr>
</table>

</details>

## Tech stack

- **UI:** Jetpack Compose, Material 3, Navigation Compose, adaptive layouts, Coil
- **Architecture:** Clean Architecture per feature, MVI-style ViewModels, use cases, repositories
- **DI:** Hilt
- **Backend:** Firebase Auth, Cloud Firestore, Firebase Storage
- **Sign-in:** Credential Manager (Google), Facebook Login
- **Other:** Kotlin Coroutines and Flow, DataStore, libphonenumber, zxcvbn (password strength)
- **Testing:** JUnit 5, MockK, Turbine, Truth, Robolectric, Compose UI tests, Konsist (architecture rules as tests)

## Project structure

```
app/src/main/java/com/shopping/shoply/
├── authentication/   # data · domain · presentation · di
├── products/
├── cart/
├── ordermanagement/
├── profile/
└── common/           # shared theme, navigation, components, DI
```

## Getting started

1. Clone the repo and open it in Android Studio.
2. To use your own Firebase project, add an Android app with the package `com.shopping.shoply` and replace `app/google-services.json` with yours. Enable Email/Password, Google and Facebook sign-in in Firebase Authentication.
3. Add the social sign-in keys to `local.properties`:
   ```properties
   GOOGLE_WEB_CLIENT_ID=your-web-client-id.apps.googleusercontent.com
   FACEBOOK_APP_ID="your-facebook-app-id"
   FACEBOOK_CLIENT_TOKEN="your-facebook-client-token"
   ```
4. Run the `app` configuration on a device or emulator (min SDK 24).

Run the unit tests with:

```
./gradlew testDebugUnitTest
```
