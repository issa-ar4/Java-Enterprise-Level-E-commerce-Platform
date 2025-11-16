# Contributing to Enterprise E-commerce Platform

Thank you for your interest in contributing to this project! This document provides guidelines and instructions for contributing.

## Table of Contents
- [Getting Started](#getting-started)
- [Development Workflow](#development-workflow)
- [Code Standards](#code-standards)
- [Testing Guidelines](#testing-guidelines)
- [Commit Message Guidelines](#commit-message-guidelines)
- [Pull Request Process](#pull-request-process)

## Getting Started

### Prerequisites
- Java 17 or higher
- Docker & Docker Compose
- Maven or Gradle
- Git
- IDE (IntelliJ IDEA, Eclipse, or VS Code)

### Setting Up Development Environment

1. **Fork the repository**
   ```bash
   # Click the 'Fork' button on GitHub
   ```

2. **Clone your fork**
   ```bash
   git clone https://github.com/YOUR_USERNAME/enterprise-ecommerce-platform.git
   cd enterprise-ecommerce-platform
   ```

3. **Add upstream remote**
   ```bash
   git remote add upstream https://github.com/ORIGINAL_OWNER/enterprise-ecommerce-platform.git
   ```

4. **Start infrastructure services**
   ```bash
   docker-compose up -d
   ```

5. **Build the project**
   ```bash
   ./mvnw clean install
   ```

## Development Workflow

1. **Create a feature branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Make your changes**
   - Write code
   - Add tests
   - Update documentation

3. **Run tests**
   ```bash
   ./mvnw test
   ```

4. **Commit your changes**
   ```bash
   git add .
   git commit -m "feat: add new feature"
   ```

5. **Push to your fork**
   ```bash
   git push origin feature/your-feature-name
   ```

6. **Create a Pull Request**
   - Go to GitHub
   - Click "New Pull Request"
   - Fill in the PR template

## Code Standards

### Java Code Style
- Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- Use meaningful variable and method names
- Maximum line length: 120 characters
- Use proper indentation (4 spaces)

### Example
```java
public class UserService {
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public User createUser(UserRequest request) {
        // Validate input
        validateUserRequest(request);
        
        // Create user
        User user = User.builder()
            .email(request.getEmail())
            .name(request.getName())
            .build();
            
        return userRepository.save(user);
    }
}
```

### Naming Conventions
- **Classes**: PascalCase (e.g., `UserService`, `OrderController`)
- **Methods**: camelCase (e.g., `createUser`, `getOrderById`)
- **Variables**: camelCase (e.g., `userId`, `orderTotal`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `MAX_RETRY_ATTEMPTS`)
- **Packages**: lowercase (e.g., `com.ecommerce.user.service`)

## Testing Guidelines

### Unit Tests
- Use JUnit 5
- Mock dependencies with Mockito
- Aim for 80%+ code coverage
- Test file naming: `*Test.java`

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserService userService;
    
    @Test
    void shouldCreateUser() {
        // Given
        UserRequest request = new UserRequest("test@example.com", "Test User");
        
        // When
        User result = userService.createUser(request);
        
        // Then
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
    }
}
```

### Integration Tests
- Use `@SpringBootTest`
- Use test containers for databases
- Test file naming: `*IntegrationTest.java`

### Test Coverage
- Minimum coverage: 80%
- Run coverage report:
  ```bash
  ./mvnw test jacoco:report
  ```

## Commit Message Guidelines

We follow [Conventional Commits](https://www.conventionalcommits.org/).

### Format
```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types
- **feat**: A new feature
- **fix**: A bug fix
- **docs**: Documentation changes
- **style**: Code style changes (formatting, etc.)
- **refactor**: Code refactoring
- **test**: Adding or updating tests
- **chore**: Maintenance tasks

### Examples
```
feat(user): add email verification

Implement email verification for new user registrations.
Sends verification email with token and validates on confirmation.

Closes #123
```

```
fix(order): resolve race condition in order creation

Fixed a race condition that occurred when multiple requests
tried to create orders simultaneously for the same user.

Fixes #456
```

## Pull Request Process

### Before Submitting
- [ ] Code follows style guidelines
- [ ] Tests have been added/updated
- [ ] All tests pass
- [ ] Documentation has been updated
- [ ] Commit messages follow guidelines
- [ ] No merge conflicts

### PR Template
```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
- [ ] Unit tests added/updated
- [ ] Integration tests added/updated
- [ ] Manual testing completed

## Checklist
- [ ] Code follows style guidelines
- [ ] Self-review completed
- [ ] Documentation updated
- [ ] No new warnings
- [ ] Tests pass locally
```

### Review Process
1. At least one approval required
2. All CI checks must pass
3. No merge conflicts
4. Documentation reviewed
5. Code review comments addressed

### Merging
- Squash and merge for feature branches
- Maintainers will merge approved PRs

## Branch Naming Convention

- Feature: `feature/description`
- Bug fix: `fix/description`
- Hotfix: `hotfix/description`
- Release: `release/version`

## Documentation

### Code Comments
- Use JavaDoc for public APIs
- Comment complex logic
- Avoid obvious comments

```java
/**
 * Creates a new user account with the provided information.
 *
 * @param request the user registration request containing email and name
 * @return the created user with generated ID
 * @throws DuplicateEmailException if email already exists
 */
public User createUser(UserRequest request) {
    // Implementation
}
```

### README Updates
- Update README for new features
- Keep getting started guide current
- Update dependencies list

## Getting Help

- GitHub Issues: Report bugs or request features
- Discussions: Ask questions or discuss ideas
- Email: contact@example.com

## Code of Conduct

- Be respectful and inclusive
- Welcome newcomers
- Focus on constructive feedback
- Respect differing opinions

## License

By contributing, you agree that your contributions will be licensed under the MIT License.

---

Thank you for contributing! 🎉
