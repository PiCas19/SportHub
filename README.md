# SportHub
**SportHub** 

SportHub is a web platform designed to solve the fragmentation between sports tracking apps and messaging services by integrating Strava data with a Telegram bot. It offers statistics, rankings, personalized goals, and social competitions.

## Group Components:
- Pierpaolo Casati
- Valmir Alimi

## 🚀 Key features
- Login with username and password.
- Register new account, receive verification email to activate new account.
- View personal information about your activities through customizable graphs.
- Active page, ability to manually add your own activities, view your activities imported from Strava.
- Goals, view goals with various information and percentage to complete, option to add and remove goals.
- Telegram page, page to view your registered groups, option to add new groups and remove existing groups.
- Challenges, all current, past, and future challenges where you can view the leaderboards for each challenge, option to participate in challenges, create new challenges.
- Settings, viewing and uploading profile photos, ability to change user passwords, personal information, ability to connect to Strava and Telegram, and finally, permanent account deletion.

## 🛠️ Technologies used
**Frontend**
- [React + Vite](https://dotnet.microsoft.com/it-it/apps/maui) — Used for the front end of our app.
- [Material UI](https://learn.microsoft.com/en-us/dotnet/maui/xaml/fundamentals/mvvm?view=net-maui-9.0) — React component library used for front-end UIs.
 
## ⚙️ Project implementation
**Configure the Frontend environment**: 

The first time you open the project, you need to install the packages: 
```bash
npm i
```
Then you can start the project with the command
```bash
npm run dev
```

**Configure the backend environment**:

First, download and install ngrok from the official website https://ngrok.com/. If you have a UNIX-based system (such as Linux or macOS), you can use the curl command to download it directly:
```bash
curl -s https://ngrok.com/download | bash
```
If you are using Windows, you can download the .zip file and follow the installation instructions.

Once installed, open a terminal and start the tunnel that exposes your application running on port 8080 (or any other port on which your backend server is listening) with the following command:

```bash
ngrok http 8080
```

This command starts a tunnel and will return a public URL similar to this:

```nginx
Forwarding                    https://abcd1234.ngrok.io -> http://localhost:8080
```

The public URL (in this example, https://abcd1234.ngrok.io) is the one you need to configure as a webhook in the Telegram bot.


Now that you have the public URL via ngrok, you need to configure the webhook for the Telegram bot. Use curl to make a POST request to the Telegram API, setting the webhook to your local server exposed via ngrok. The complete command will be:

```bash
curl -F "url=https://abcd1234.ngrok.io/api/telegram/update" https://api.telegram.org/bot<YourBotToken>/setWebhook
```