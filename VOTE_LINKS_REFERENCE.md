# Vote Links - Quick Reference

## 🎯 Vote Sites Summary

| Site             | Site ID / Username | Base Link                                                   | Format                                   |
| ---------------- | ------------------ | ----------------------------------------------------------- | ---------------------------------------- |
| **GTOP100**      | 105528             | `https://gtop100.com/MapleStory/server-105528`              | `-USERNAME?vote=1&pingUsername=USERNAME` |
| **TopG**         | 678265             | `https://topg.org/maplestory-private-servers/server-678265` | `-USERNAME#vote`                         |
| **XtremeTop100** | (Get from site)    | `https://www.xtremetop100.com/in.php`                       | `?site=SITEID&postback=USERNAME`         |
| **Arena-Top100** | cloudstory         | `https://www.arena-top100.com/index.php`                    | `?a=in&u=USERNAME&id=PLAYERID`           |

---

## 📝 How to Build Vote Links in Code

When a player with username `Dragon` clicks the vote button, you send them to:

### GTOP100

```
https://gtop100.com/MapleStory/server-105528-Dragon?vote=1&pingUsername=Dragon
```

### TopG

```
https://topg.org/maplestory-private-servers/server-678265-Dragon#vote
```

### XtremeTop100

```
https://www.xtremetop100.com/in.php?site=SITEID&postback=Dragon
```

(Replace SITEID with your actual Site ID from XtremeTop100)

### Arena-Top100

```
https://www.arena-top100.com/index.php?a=in&u=cloudstory&id=Dragon
```

(Use "cloudstory" as the username, replace "Dragon" with the player's username/ID)

---

## 💻 Frontend Code Example (JavaScript/React)

```javascript
const username = "Dragon"; // Get from logged-in user

const voteLinks = {
  gtop100: `https://gtop100.com/MapleStory/server-105528-${username}?vote=1&pingUsername=${username}`,
  topg: `https://topg.org/maplestory-private-servers/server-678265-${username}#vote`,
  xtremetop100: `https://www.xtremetop100.com/in.php?site=YOUR_SITEID&postback=${username}`, // Replace YOUR_SITEID
  arenaTop100: `https://www.arena-top100.com/index.php?a=in&u=cloudstory&id=${username}`,
};

// When user clicks "Vote TopG" button:
window.open(voteLinks.topg, "_blank");
```

---

## 🔑 Backend Keys (Already Set)

```properties
vote.secret.gtop100=CloudStorySecretKey2026
vote.secret.topg=YOUR_KEY_HERE (pending)
vote.secret.xtremetop100=YOUR_KEY_HERE (pending)
vote.secret.arena-top100=YOUR_KEY_HERE (pending)
```

---

## 📌 Summary for Frontend

1. Get the **logged-in username** from the player
2. Build the vote link by **replacing `USERNAME` with their actual username**
3. Open the link in a **new tab** (using `window.open()`)
4. After they vote, the site sends us a callback
5. We check the secret key and reward them automatically

---

## 🔗 Backend Callback Endpoints

```
POST /api/vote/callback/gtop100
POST /api/vote/callback/topg
POST /api/vote/callback/xtremetop100
POST /api/vote/callback/arena-top100
```

Each endpoint receives a callback from the voting site after a successful vote and automatically rewards the player with NX and Vote Points (multiplied by their tier).
