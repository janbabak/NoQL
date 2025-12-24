# React + TypeScript + Vite

## Build docker image

- Build image without pushing
  ```bash
  npm run buildFrontendDockerImage
  ```

- Build and push
  ```bash
  npm run buildAndPushDockerImage
  ```

## Docker image

- **How to run:**
    - Portforward port desired port to `80` in container
    - Define necessary environment variables
  ```bash
  docker run -p 80:80 \                                                                                       ✔  25s   10:13:02  
  --env-file ./frontend/NoQL/.env \
  --name noql-frontend \
  janbabak/noql-frontend:0.0.1
  ```

This template provides a minimal setup to get React working in Vite with HMR and some ESLint rules.

Currently, two official plugins are available:

- [@vitejs/plugin-react](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react/README.md)
  uses [Babel](https://babeljs.io/) for Fast Refresh
- [@vitejs/plugin-react-swc](https://github.com/vitejs/vite-plugin-react-swc) uses [SWC](https://swc.rs/) for Fast
  Refresh

## Expanding the ESLint configuration

If you are developing a production application, we recommend updating the configuration to enable type aware lint rules:

- Configure the top-level `parserOptions` property like this:

```js
export default {
  // other rules...
  parserOptions: {
    ecmaVersion: 'latest',
    sourceType: 'module',
    project: ['./tsconfig.json', './tsconfig.node.json'],
    tsconfigRootDir: __dirname,
  },
}
```

- Replace `plugin:@typescript-eslint/recommended` to `plugin:@typescript-eslint/recommended-type-checked` or
  `plugin:@typescript-eslint/strict-type-checked`
- Optionally add `plugin:@typescript-eslint/stylistic-type-checked`
- Install [eslint-plugin-react](https://github.com/jsx-eslint/eslint-plugin-react) and add `plugin:react/recommended` &
  `plugin:react/jsx-runtime` to the `extends` list
