
import .io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        VotingSystem votingSystem = new VotingSystem();
        votingSystem.run();
    }
}

// Модель
class VotingSystem {

    private List<User> users = new ArrayList<>();
    private List<Candidate> candidates = new ArrayList<>();
    private List<Election> elections = new ArrayList<>();
    private User loggedInUser = null;
    private Scanner scanner = new Scanner(System.in);

    public void run() {
        loadData();
        while (true) {
            if (loggedInUser == null) {
                showLoginRegisterMenu();
            } else {
                showMainMenu();
            }
        }
    }

    private void showLoginRegisterMenu() {
        System.out.println("\n--- Меню ---");
        System.out.println("1. Войти");
        System.out.println("2. Зарегистрироваться");
        System.out.println("0. Выход");

        System.out.print("Выберите действие: ");
        String choice = scanner.nextLine();

        switch (choice) {
            case "1":
                login();
                break;
            case "2":
                register();
                break;
            case "0":
                System.out.println("Выход из системы.");
                saveData();
                System.exit(0);
            default:
                System.out.println("Некорректный ввод.");
        }
    }

    private void showMainMenu() {
        System.out.println("\n--- Главное меню ---");
        System.out.println("Вы вошли как: " + loggedInUser.getRole());

        switch (loggedInUser.getRole()) {
            case ADMINISTRATOR:
                showAdminMenu();
                break;
            case CIC:
                showCicMenu();
                break;
            case CANDIDATE:
                showCandidateMenu();
                break;
            case USER:
                showUserMenu();
                break;
        }
    }

    private void showAdminMenu() {
        System.out.println("1. Просмотр и удаление пользователей");
        System.out.println("2. Просмотр и удаление ЦИК");
        System.out.println("3. Создание ЦИК (логин:пароль)");
        System.out.println("4. Просмотр и удаление кандидатов");
        System.out.println("0. Выйти");

        System.out.print("Выберите действие: ");
        String choice = scanner.nextLine();

        switch (choice) {
            case "1":
                viewAndDeleteUsers();
                break;
            case "2":
                viewAndDeleteCICs();
                break;
            case "3":
                createCIC();
                break;
            case "4":
                viewAndDeleteCandidates();
                break;
            case "0":
                logout();
                break;
            default:
                System.out.println("Некорректный ввод.");
        }
    }

    private void showCicMenu() {
        System.out.println("1. Создание голосования с окончанием по дате");
        System.out.println("2. Добавление кандидатов (логин:пароль)");
        System.out.println("3. Печать результатов (PDF - заглушка)");
        System.out.println("4. Выбор группировки результатов (заглушка)");
        System.out.println("5. Сортировка результатов (заглушка)");
        System.out.println("0. Выйти");

        System.out.print("Выберите действие: ");
        String choice = scanner.nextLine();

        switch (choice) {
            case "1":
                createElection();
                break;
            case "2":
                addCandidate();
                break;
            case "3":
                System.out.println("Функциональность печати результатов в PDF не реализована.");
                break;
            case "4":
                System.out.println("Функциональность группировки результатов не реализована.");
                break;
            case "5":
                System.out.println("Функциональность сортировки результатов не реализована.");
                break;
            case "0":
                logout();
                break;
            default:
                System.out.println("Некорректный ввод.");
        }
    }

    private void showCandidateMenu() {
        System.out.println("1. Заполнение данных о себе (заглушка)");
        System.out.println("2. Результаты предыдущего голосования (заглушка)");
        System.out.println("3. Все голосования, в которых принимал участие (заглушка)");
        System.out.println("0. Выйти");

        System.out.print("Выберите действие: ");
        String choice = scanner.nextLine();

        switch (choice) {
            case "1":
                System.out.println("Функциональность заполнения данных не реализована.");
                break;
            case "2":
                System.out.println("Функциональность просмотра результатов не реализована.");
                break;
            case "3":
                System.out.println("Функциональность просмотра голосований не реализована.");
                break;
            case "0":
                logout();
                break;
            default:
                System.out.println("Некорректный ввод.");
        }
    }

    private void showUserMenu() {
        System.out.println("1. Просмотр списка кандидатов");
        System.out.println("2. Голосование");
        System.out.println("3. Все голосования, в которых голосовал/не голосовал (заглушка)");
        System.out.println("0. Выйти");

        System.out.print("Выберите действие: ");
        String choice = scanner.nextLine();

        switch (choice) {
            case "1":
                viewCandidates();
                break;
            case "2":
                vote();
                break;
            case "3":
                System.out.println("Функциональность просмотра голосований не реализована.");
                break;
            case "0":
                logout();
                break;
            default:
                System.out.println("Некорректный ввод.");
        }
    }

    private void login() {
        System.out.print("Введите логин: ");
        String login = scanner.nextLine();
        System.out.print("Введите пароль: ");
        String password = scanner.nextLine();

        for (User user : users) {
            if (user.getLogin().equals(login) && user.getPassword().equals(password)) {
                loggedInUser = user;
                System.out.println("Успешный вход.");
                return;
            }
        }

        System.out.println("Неверный логин или пароль.");
    }

    private void register() {
        System.out.print("Введите ФИО: ");
        String fio = scanner.nextLine();
        System.out.print("Введите дату рождения (dd.MM.yyyy): ");
        String birthDateStr = scanner.nextLine();

        Date birthDate;
        try {
            birthDate = new SimpleDateFormat("dd.MM.yyyy").parse(birthDateStr);
        } catch (ParseException e) {
            System.out.println("Некорректный формат даты.");
            return;
        }

        System.out.print("Введите логин: ");
        String login = scanner.nextLine();
        System.out.print("Введите пароль: ");
        String password = scanner.nextLine();

        User newUser = new User(generateId(), login, password, Role.USER, fio, birthDate, null, null); //  уникальные данные
        users.add(newUser);
        System.out.println("Регистрация прошла успешно.");
    }

    private void logout() {
        loggedInUser = null;
        System.out.println("Вы вышли из системы.");
    }

    // Admin functions
    private void viewAndDeleteUsers() {
        if (loggedInUser.getRole() != Role.ADMINISTRATOR) {
            System.out.println("У вас нет прав для выполнения этого действия.");
            return;
        }

        System.out.println("\n--- Список пользователей ---");
        for (int i = 0; i < users.size(); i++) {
            System.out.println((i + 1) + ". " + users.get(i));
        }

        System.out.print("Введите номер пользователя для удаления (или 0 для отмены): ");
        String choice = scanner.nextLine();

        try {
            int index = Integer.parseInt(choice) - 1;
            if (index >= 0 && index < users.size() && users.get(index).getRole() != Role.ADMINISTRATOR) {
                users.remove(index);
                System.out.println("Пользователь удален.");
            } else if (index >= 0 && index < users.size() && users.get(index).getRole() == Role.ADMINISTRATOR) {
                System.out.println("Нельзя удалять администраторов.");
            }
            else if (choice.equals("0")) {
                return;
            } else {
                System.out.println("Некорректный номер.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Некорректный ввод.");
        }
    }

    private void viewAndDeleteCICs() {
        if (loggedInUser.getRole() != Role.ADMINISTRATOR) {
            System.out.println("У вас нет прав для выполнения этого действия.");
            return;
        }

        System.out.println("\n--- Список ЦИК ---");
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getRole() == Role.CIC) {
                System.out.println((i + 1) + ". " + users.get(i));
            }
        }

        System.out.print("Введите номер ЦИК для удаления (или 0 для отмены): ");
        String choice = scanner.nextLine();

        try {
            int index = Integer.parseInt(choice) - 1;
            if (index >= 0 && index < users.size() && users.get(index).getRole() == Role.CIC) {
                users.remove(index);
                System.out.println("ЦИК удален.");
            } else if (choice.equals("0")){
                return;
            }
            else {
                System.out.println("Некорректный номер.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Некорректный ввод.");
        }
    }

    private void createCIC() {
        if (loggedInUser.getRole() != Role.ADMINISTRATOR) {
            System.out.println("У вас нет прав для выполнения этого действия.");
            return;
        }

        System.out.print("Введите логин для нового ЦИК: ");
        String login = scanner.nextLine();
        System.out.print("Введите пароль для нового ЦИК: ");
        String password = scanner.nextLine();

        User newCic = new User(generateId(), login, password, Role.CIC, "ЦИК", null, null, null);
        users.add(newCic);
        System.out.println("ЦИК создан.");
    }

    private void viewAndDeleteCandidates() {
        if (loggedInUser.getRole() != Role.ADMINISTRATOR) {
            System.out.println("У вас нет прав для выполнения этого действия.");
            return;
        }

        System.out.println("\n--- Список кандидатов ---");
        for (int i = 0; i < candidates.size(); i++) {
            System.out.println((i + 1) + ". " + candidates.get(i));
        }

        System.out.print("Введите номер кандидата для удаления (или 0 для отмены): ");
        String choice = scanner.nextLine();

        try {
            int index = Integer.parseInt(choice) - 1;
            if (index >= 0 && index < candidates.size()) {
                candidates.remove(index);
                System.out.println("Кандидат удален.");
            } else if (choice.equals("0"))
            {
                return;
            } else {
                System.out.println("Некорректный номер.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Некорректный ввод.");
        }
    }


    // CIC functions
    private void createElection() {
        if (loggedInUser.getRole() != Role.CIC) {
            System.out.println("У вас нет прав для выполнения этого действия.");
            return;
        }

        System.out.print("Введите дату окончания голосования (dd.MM.yyyy HH:mm): ");
        String endDateStr = scanner.nextLine();

        Date endDate;
        try {
            endDate = new SimpleDateFormat("dd.MM.yyyy HH:mm").parse(endDateStr);
        } catch (ParseException e) {
            System.out.println("Некорректный формат даты.");
            return;
        }

        Election newElection = new Election(generateId(), endDate);
        elections.add(newElection);
        System.out.println("Голосование создано.");
    }

    private void addCandidate() {
        if (loggedInUser.getRole() != Role.CIC) {
            System.out.println("У вас нет прав для выполнения этого действия.");
            return;
        }

        System.out.print("Введите логин кандидата: ");
        String login = scanner.nextLine();
        System.out.print("Введите пароль кандидата: ");
        String password = scanner.nextLine();

        Candidate newCandidate = new Candidate(generateId(), login, password);
        candidates.add(newCandidate);
        System.out.println("Кандидат добавлен.");

        User newUser = new User(generateId(), login, password, Role.CANDIDATE, "Кандидат", null, null, null);
        users.add(newUser);
        System.out.println("Пользователь-кандидат добавлен.");
    }


    // User functions
    private void viewCandidates() {
        System.out.println("\n--- Список кандидатов ---");
        for (Candidate candidate : candidates) {
            System.out.println(candidate);
        }
    }

    private void vote() {
        if (loggedInUser.getRole() != Role.USER) {
            System.out.println("У вас нет прав для выполнения этого действия.");
            return;
        }

        System.out.println("\n--- Доступные голосования ---");
        if (elections.isEmpty()) {
            System.out.println("Нет активных голосований.");
            return;
        }

        for (int i = 0; i < elections.size(); i++) {
            System.out.println((i + 1) + ". " + elections.get(i));
        }

        System.out.print("Введите номер голосования, в котором хотите участвовать (или 0 для отмены): ");
        String electionChoice = scanner.nextLine();

        try {
            int electionIndex = Integer.parseInt(electionChoice) - 1;
            if (electionIndex >= 0 && electionIndex < elections.size()) {
                Election selectedElection = elections.get(electionIndex);

                System.out.println("\n--- Список кандидатов для голосования ---");
                for (int i = 0; i < candidates.size(); i++) {
                    System.out.println((i + 1) + ". " + candidates.get(i));
                }

                System.out.print("Введите номер кандидата, за которого хотите проголосовать (или 0 для отмены): ");
                String candidateChoice = scanner.nextLine();

                try {
                    int candidateIndex = Integer.parseInt(candidateChoice) - 1;
                    if (candidateIndex >= 0 && candidateIndex < candidates.size()) {
                        Candidate selectedCandidate = candidates.get(candidateIndex);
                        selectedElection.addVote(selectedCandidate);
                        System.out.println("Ваш голос принят.");
                    } else if(candidateChoice.equals("0")) {
                        return;
                    }
                    else {
                        System.out.println("Некорректный номер кандидата.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Некорректный ввод.");
                }
            } else if (electionChoice.equals("0")) {
                return;
            }
            else {
                System.out.println("Некорректный номер голосования.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Некорректный ввод.");
        }
    }

    private int generateId() {
        return new Random().nextInt(1000);
    }

    // Data persistence (Заглушки, необходима реализация сохранения и загрузки данных в файл)
    private void loadData() {
        // Заглушка - необходимо реализовать загрузку данных из файла
        System.out.println("Загрузка данных...");

        // Создаем тестовых пользователей
        User admin = new User(generateId(), "admin", "admin", Role.ADMINISTRATOR, "Администратор", null, null, null);
        users.add(admin);

        User cic = new User(generateId(), "cic", "cic", Role.CIC, "ЦИК", null, null, null);
        users.add(cic);

        User user1 = new User(generateId(), "user1", "user1", Role.USER, "Пользователь 1", null, null, null);
        users.add(user1);

    }

    private void saveData() {
        // Заглушка - необходимо реализовать сохранение данных в файл
        System.out.println("Сохранение данных...");
    }
}

// Роли пользователей
enum Role {
    ADMINISTRATOR,
    CIC,
    CANDIDATE,
    USER
}

// Класс пользователя
class User {

    private int id;
    private String login;
    private String password;
    private Role role;
    private String fio;
    private Date birthDate;
    private String snils;
    private String uniqueData; // Какие-то уникальные данные

    public User(int id, String login, String password, Role role, String fio, Date birthDate, String snils, String uniqueData) {
        this.id = id;
        this.login = login;
        this.password = password;
        this.role = role;
        this.fio = fio;
        this.birthDate = birthDate;
        this.snils = snils;
        this.uniqueData = uniqueData;
    }

    public int getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }

    @Override
    public String toString() {
        return "User{" + "id=" + id + ", login='" + login + '\'' + ", role=" + role + ", fio='" + fio + '\'' + '}';
    }
}

// Класс кандидата
class Candidate {

    private int id;
    private String login;
    private String password;

    public Candidate(int id, String login, String password) {
        this.id = id;
        this.login = login;
        this.password = password;
    }

    public int getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    @Override
    public String toString() {
        return "Candidate{" + "id=" + id + ", login='" + login + '\'' + '}';
    }
}

// Класс выборов
class Election {

    private int id;
    private Date endDate;
    private Map<Candidate, Integer> votes = new HashMap<>();

    public Election(int id, Date endDate) {
        this.id = id;
        this.endDate = endDate;
    }

    public int getId() {
        return id;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void addVote(Candidate candidate) {
        votes.put(candidate, votes.getOrDefault(candidate, 0) + 1);
    }

    public Map<Candidate, Integer> getVotes() {
        return votes;
    }

    @Override
    public String toString() {
        return "Election{" + "id=" + id + ", endDate=" + new SimpleDateFormat("dd.MM.yyyy HH:mm").format(endDate) + '}';
    }
}
