# Arena-Top100 Postback Diagnostic Guide

## ❌ Problem Detected

**You voted on Arena-Top100 but NO postback/callback was received by your backend.**

### Backend Status

- ✅ TopG: **Working** (callbacks received)
- ✅ GTOP100: **Working** (tested)
- ✅ XtremeTop100: **Working** (tested)
- ❌ Arena-Top100: **NOT WORKING** (no callback received)

---

## 🔍 How to Diagnose

### Step 1: Verify the Vote Was Actually Recorded on Arena-Top100

Go to: https://www.arena-top100.com/

Look for CloudStory server in the list. Check if:

- ✅ Vote count increased
- ✅ Your character name appears in recent voters list
- ❌ If not visible, the vote may not have been processed on Arena-Top100's end

---

### Step 2: Check Your Callback URL Configuration

Your current Arena-Top100 callback URL should be:

```
http://72.60.127.22:8080/api/vote/callback/arena-top100
```

**Log into Arena-Top100 admin panel and verify:**

1. Go to your server management page
2. Look for "Callback URL" or "Postback URL" setting
3. Make sure it matches exactly: `http://72.60.127.22:8080/api/vote/callback/arena-top100`

---

### Step 3: Check Firewall & Port Access

Your backend is behind a firewall on port 8080. Arena-Top100 might not be able to reach it.

**Check if port 8080 is accessible from outside:**

```bash
# From your server terminal:
ssh root@72.60.127.22 "netstat -tlnp | grep 8080"
```

**Result should show:**

```
tcp  0  0  0.0.0.0:8080  0.0.0.0:*  LISTEN
```

If not listening on 0.0.0.0, you need to check Docker container port mapping.

---

### Step 4: Test Manual Postback

Test if your backend can receive a postback by sending a manual test:

```bash
curl -X POST "http://72.60.127.22:8080/api/vote/callback/arena-top100" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "secret": "TEST_OR_YOUR_API_SECRET",
    "voted": "1",
    "userip": "1.2.3.4",
    "userid": "123"
  }'
```

**Expected response:** Should create a test vote record (or reject if username doesn't exist)

---

### Step 5: Check Arena-Top100 Documentation

Arena-Top100 requires specific parameters. Verify in their admin panel:

**Parameters Arena-Top100 SHOULD send:**

- `secret` - Your API secret key (from application.properties)
- `voted` - "1" for success, "0" for duplicate/failed
- `userip` - Player's IP address
- `userid` - Player's character name

**Callback method:** Usually POST via query parameters or JSON body

---

## 🔧 Most Likely Causes

### 1️⃣ **Callback URL Not Configured in Arena-Top100 Admin Panel**

**Solution:** Log into Arena-Top100 server management → Set callback URL exactly as: `http://72.60.127.22:8080/api/vote/callback/arena-top100`

### 2️⃣ **Wrong Secret Key**

**Current:** `TEST_OR_YOUR_API_SECRET` (in application.properties)  
**Solution:** Replace with the actual API secret from Arena-Top100 admin panel

### 3️⃣ **Vote Failed on Arena-Top100's End**

**Solution:** Check Arena-Top100 website directly to confirm the vote was recorded

### 4️⃣ **Port 8080 Not Accessible from Outside**

**Solution:** Check firewall rules and Docker port forwarding

### 5️⃣ **Wrong Parameter Format**

**Your backend expects:**

```
POST /api/vote/callback/arena-top100
```

With these parameters (any format):

- `secret` - String
- `voted` - String ("1" or "0")
- `username` - String (character name)
- `userip` - String (IP address)
- `userid` - String

---

## 📋 Verification Checklist

- [ ] Go to Arena-Top100 website and confirm vote was recorded there
- [ ] Log into Arena-Top100 admin panel
- [ ] Find server callback/postback URL setting
- [ ] Verify it's set to: `http://72.60.127.22:8080/api/vote/callback/arena-top100`
- [ ] Get your actual API secret from Arena-Top100
- [ ] Update `vote.secret.arena-top100=YOUR_ACTUAL_SECRET` in application.properties
- [ ] Restart backend application
- [ ] Vote again on Arena-Top100
- [ ] Check backend logs for Arena-Top100 callback activity
- [ ] Verify vote appears in database

---

## 🔑 Current Configuration

**application.properties:**

```properties
vote.secret.arena-top100=TEST_OR_YOUR_API_SECRET
```

**Change to:**

```properties
vote.secret.arena-top100=<your_actual_api_secret_from_arena_admin_panel>
```

---

## 📞 Next Steps

1. **Check Arena-Top100 Admin Panel** for callback URL and actual API secret
2. **Update application.properties** with correct secret key
3. **Restart Docker container** to load new configuration
4. **Vote again** on Arena-Top100
5. **Check logs** for postback activity: `ssh root@72.60.127.22 "docker logs cloudstory-site-server | grep arena"`

---

## 💡 How Postback Works (Expected Flow)

```
1. Player visits your vote link
2. Arena-Top100 records the vote
3. Arena-Top100 sends HTTP callback to: http://72.60.127.22:8080/api/vote/callback/arena-top100
4. Your backend receives callback → Validates secret key → Checks voted flag
5. Your backend records vote in database → Calculates rewards → Updates tier
6. Player sees success message
```

If Step 3 doesn't happen, no vote is recorded.
