# Voting Sites Setup Guide

This guide explains how to register your server on the supported voting sites and configure the "Callback" (or Postback) so your server knows when a player votes.

## 📋 General Information

- **Your Website URL:** `https://cloudstory.online`
- **Your Callback Base URL:** `https://cloudstory.online/api/vote/callback/`

---

## 1. GTOP100 (Most Important)

#### 1. GTOP100 (Most Important)

- **Website:** [gtop100.com](https://gtop100.com)
- **Callback URL to enter:** `https://cloudstory.online/api/vote/callback/gtop100`
- **Important:** When you create the vote link in GTOP100, add `&pingUsername=USERNAME` to let them know what username to track.
- **Example Vote Link:** `https://gtop100.com/MapleStory/server-105528-{USERNAME}?vote=1&pingUsername={USERNAME}`
- **Action:** Create a **Pingback Key** in GTOP100 settings and paste it into `application.properties` at `vote.secret.gtop100`.

---

## 2. TopG

1.  **Register:** Go to [topg.org](https://topg.org) and sign up.
2.  **Add Server:** Add your MapleStory server.
3.  **Configure Postback:**
    - Go to "Server Settings" -> "Voting API" or "Postback".
    - **Postback URL:** `https://cloudstory.online/api/vote/callback/topg`
    - _Note: TopG will automatically append `?p_resp=USERNAME&ip=USER_IP` when calling your callback._
4.  **Get Secret Key:**
    - Find the **Callback Key** or **Secret** in the API section.
5.  **Update Backend:**
    - Set `vote.secret.topg=YOUR_COPIED_KEY` in `application.properties`.

---

## 3. XtremeTop100

## 3. XtremeTop100

1.  **Register:** Go to [xtremetop100.com](https://xtremetop100.com).
2.  **Add Server:** Submit your server and note your **Site ID**.
3.  **Configure Postback:**
    - Go to "Edit Postback" in your dashboard (requires Gold Membership).
    - **Postback URL:** `https://cloudstory.online/api/vote/callback/xtremetop100`
    - _Note: XtremeTop100 automatically appends `?custom=USERNAME&votingip=IP_ADDRESS`_
4.  **Get Postback Password:**
    - In the Postback settings, find or set your **Postback Password**.
    - Copy this password.
5.  **Update Backend:**
    - Set `vote.secret.xtremetop100=YOUR_POSTBACK_PASSWORD` in `application.properties`.
6.  **Update Vote Link:**
    - Your vote link should be: `https://www.xtremetop100.com/in.php?site=SITEID&postback=USERNAME`
    - (Replace SITEID with your Site ID and USERNAME with the player's username)

---

## 4. Arena-Top100

1.  **Register:** Go to [arena-top100.com](https://arena-top100.com).
2.  **Add Server:** Submit your server.
3.  **Configure Postback:**
    - Look for "Vote Callback" settings.
    - **Postback URL:** `https://cloudstory.online/api/vote/callback/arena-top100?username={USERNAME}&key={KEY}`
4.  **Get Secret Key:**
    - Copy the Secret Key.
5.  **Update Backend:**
    - Set `vote.secret.arena-top100=YOUR_COPIED_KEY` in `application.properties`.

---

## 🚀 Final Steps

1.  **Save Keys:** Make sure all keys are pasted into `src/main/resources/application.properties`.
2.  **GTOP100 Special:**
    - The Pingback URL should be: `https://cloudstory.online/api/vote/callback/gtop100` (with no parameters)
    - Create a **Pingback Key** in their settings
    - Your vote links should include `&pingUsername=USERNAME` so GTOP100 knows which player voted
3.  **Deploy:** Commit and push your changes so the backend updates on the VPS.
4.  **Get Vote Links:**
    - After adding your server, each site will give you a **Vote Link** (e.g., `https://gtop100.com/MapleStory/server-105528-vote`).
    - **For GTOP100:** Modify it to include `&pingUsername=USERNAME` so players can be tracked.
    * **Save these links!** You will need them for the Frontend "Vote" page buttons.
