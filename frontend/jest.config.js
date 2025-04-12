module.exports = {
  preset: '@vue/cli-plugin-unit-jest',
  transform: {
    '^.+\\.vue$': '@vue/vue3-jest',
    '^.+\\.jsx?$': 'babel-jest'
  },
  testMatch: [
    '**/tests/unit/**/*.spec.[jt]s?(x)',
    '**/__tests__/*.[jt]s?(x)'
  ],
  testEnvironment: 'jsdom'
}
