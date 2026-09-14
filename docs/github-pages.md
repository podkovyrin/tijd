# Privacy policy on GitHub Pages

Tijd uses its GitHub repository for source and GitHub Pages for a single public
privacy-policy document. No separate domain or marketing website is needed.
The app also includes the complete text under **Privacy policy**, readable offline.

## Current state

- Policy drafted in `android/app/src/main/res/raw/privacy_policy.txt`.
- Android's Privacy policy button reads that file directly.
- `scripts/build_privacy_page.py` renders the same file as accessible HTML with
  local assets, no JavaScript, no analytics, and no external fonts.
- `.github/workflows/privacy-pages.yml` builds and publishes **only when run
  manually from `main`**. A push does not publish the page.
- No Git remote is configured yet. Nothing has been pushed or deployed.
- The GitHub owner/repository and public privacy contact still need confirmation.
  CI publication fails until the policy has a **Privacy questions** section.

## Before publication

1. Choose the GitHub owner/repository. This determines the public Pages URL.
2. Add a final section titled `Privacy questions` to the policy source, followed
   by a paragraph with the actual contact mechanism. Use a monitored public email
   address, or the repository's full GitHub Issues URL if that is the chosen
   support channel. For Issues, explain that posts are public and ask people not
   to include sensitive information. Describe handling/retention of voluntarily
   submitted support information, separate from the app's offline behavior.
3. Add the repository URL to the policy's GitHub section and the root README.
4. Regenerate the page, review the text, and rebuild the app so the same final
   contact information appears in both places.

## Local preview

From the repository root, with Python 3 installed:

```sh
python3 scripts/build_privacy_page.py
python3 -m http.server 8765 --bind 127.0.0.1 --directory build/privacy-site
```

Open [the local privacy page](http://127.0.0.1:8765/privacy-policy.html).
Generated output is ignored by Git; the source, template, workflow, and branding
assets are tracked. The policy's format is intentionally simple: the first block
contains its title and update date; each subsequent block has one heading line
and its paragraph, separated from the next block by a blank line.

## First publication, after we push

1. Create the intended GitHub repository and push `main` when ready. A public
   repository can use GitHub Pages on GitHub Free; private-repository Pages
   availability depends on the GitHub plan.
2. In the repository, open **Settings → Pages → Build and deployment** and choose
   **GitHub Actions** as the source. Leave custom domain unset. Ensure HTTPS is
   enabled when GitHub exposes that option.
3. Open **Actions → Publish privacy policy → Run workflow**, choose **main**, and
   run it. The `github-pages` environment may require approval if configured.
4. Wait for both build and deploy jobs to succeed. Open the deployment URL shown
   by the workflow or Settings → Pages. Verify it is readable without signing in.
5. Use the final URL ending in `/privacy-policy.html` in Google Play's privacy
   policy field. For a normal project repository this is
   `https://OWNER.github.io/REPOSITORY/privacy-policy.html`; use the actual URL
   GitHub reports, particularly for a user/organization Pages repository.
6. Check the title, contact link, icon, plain-text download, and mobile readability.
   In the app, check **Privacy policy**, scrolling, and the contact link offline.

The workflow publishes only `build/privacy-site`, not the app source, internal
release checklist, or other repository files. It uses GitHub's short-lived token
and Pages permissions; no personal access token or custom secrets are required.

## Policy updates

Edit the bundled text, update its date when appropriate, rebuild and review,
commit/push, then manually run **Publish privacy policy** again. Ship a new app
build when the bundled policy changes. Keep the `/privacy-policy.html` path stable
so the Play Console link continues to work.

If publication fails, inspect the workflow log first. Confirm the privacy contact
is filled in, Pages uses GitHub Actions, the workflow runs from `main`, and the
repository's Actions/Pages permissions permit deployment.

References: [GitHub Pages configuration](https://docs.github.com/en/pages/getting-started-with-github-pages/configuring-a-publishing-source-for-your-github-pages-site),
[custom Pages workflows](https://docs.github.com/en/pages/getting-started-with-github-pages/using-custom-workflows-with-github-pages),
and [Google Play's User Data policy](https://support.google.com/googleplay/android-developer/answer/10144311).
