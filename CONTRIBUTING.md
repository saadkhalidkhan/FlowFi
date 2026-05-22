# Contributing to FlowFi

## Quick PR workflow (solo / no review required)

On your own fork or repo with an unprotected `master` branch:

```bash
git checkout master
git pull origin master
git checkout -b feature/your-change

# make changes, then:
git add .
git commit -m "Your message"
git push -u origin feature/your-change

gh pr create --base master --head feature/your-change --title "Your PR title" --body "Summary of changes."
gh pr merge --merge

git checkout master
git pull origin master
```

You can also open and merge the PR from the GitHub web UI without requesting reviewers.
