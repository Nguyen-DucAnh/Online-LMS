/**
 * Online-LMS Main JavaScript
 */

// Global State
let allCourses = [];
let filteredCourses = [];
let currentPage = 1;
const itemsPerPage = 8;

// DOM Elements
const courseGrid = document.getElementById('courseGrid');
const pagination = document.getElementById('pagination');
const searchInput = document.getElementById('searchInput');
const categoryFilter = document.getElementById('categoryFilter');
const levelFilter = document.getElementById('levelFilter');
const sortFilter = document.getElementById('sortFilter');
const heroSearchInput = document.getElementById('heroSearchInput');
const heroSearchBtn = document.getElementById('heroSearchBtn');

// Initialize App
document.addEventListener('DOMContentLoaded', () => {
    if (courseGrid) {
        fetchCourses();
        setupEventListeners();
    }
    setupCounterAnimation();
});

/**
 * Fetch courses from API
 */
async function fetchCourses() {
    try {
        const response = await fetch('https://glasses-ecommerce.azurewebsites.net/api/products');
        if (!response.ok) throw new Error('Failed to fetch data');
        
        const data = await response.json();
        
        // Map API data to course format
        allCourses = data.items.map(item => {
            // Randomly assign level and duration for demo purposes
            const levels = ['Beginner', 'Intermediate', 'Advanced'];
            const randomLevel = levels[Math.floor(Math.random() * levels.length)];
            const randomDuration = Math.floor(Math.random() * 40 + 5) + ' hours';
            
            return {
                id: item.id,
                title: item.productName,
                instructor: item.brand,
                category: item.category ? item.category.name : 'Uncategorized',
                level: randomLevel,
                duration: randomDuration,
                price: item.minPrice,
                listedPrice: item.maxPrice > item.minPrice ? item.maxPrice : null,
                thumbnail: item.firstImage ? item.firstImage.imageUrl : 'https://picsum.photos/seed/course/400/300',
                popularity: item.totalQuantityAvailable, // Using quantity as popularity proxy
                dateAdded: new Date(Date.now() - Math.floor(Math.random() * 10000000000)) // Random past date
            };
        });

        filteredCourses = [...allCourses];
        
        populateCategories();
        applyFiltersAndSort();
        
    } catch (error) {
        console.error('Error fetching courses:', error);
        if (courseGrid) {
            courseGrid.innerHTML = `
                <div class="col-12 text-center py-5 text-danger">
                    <i class="fas fa-exclamation-triangle fa-3x mb-3"></i>
                    <h4>Failed to load courses</h4>
                    <p>Please try again later.</p>
                </div>
            `;
        }
    }
}

/**
 * Setup Event Listeners
 */
function setupEventListeners() {
    if (searchInput) {
        searchInput.addEventListener('input', () => {
            currentPage = 1;
            applyFiltersAndSort();
        });
    }

    if (categoryFilter) {
        categoryFilter.addEventListener('change', () => {
            currentPage = 1;
            applyFiltersAndSort();
        });
    }

    if (levelFilter) {
        levelFilter.addEventListener('change', () => {
            currentPage = 1;
            applyFiltersAndSort();
        });
    }

    if (sortFilter) {
        sortFilter.addEventListener('change', () => {
            currentPage = 1;
            applyFiltersAndSort();
        });
    }

    if (heroSearchBtn && heroSearchInput) {
        heroSearchBtn.addEventListener('click', () => {
            if (searchInput) {
                searchInput.value = heroSearchInput.value;
                document.getElementById('courses').scrollIntoView({ behavior: 'smooth' });
                currentPage = 1;
                applyFiltersAndSort();
            }
        });

        heroSearchInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                heroSearchBtn.click();
            }
        });
    }
}

/**
 * Populate Category Dropdown
 */
function populateCategories() {
    if (!categoryFilter) return;
    
    const categories = [...new Set(allCourses.map(c => c.category))];
    
    // Keep the "All Categories" option
    categoryFilter.innerHTML = '<option value="">All Categories</option>';
    
    categories.forEach(category => {
        const option = document.createElement('option');
        option.value = category;
        option.textContent = category;
        categoryFilter.appendChild(option);
    });
}

/**
 * Apply Filters and Sort
 */
function applyFiltersAndSort() {
    const searchTerm = searchInput ? searchInput.value.toLowerCase() : '';
    const selectedCategory = categoryFilter ? categoryFilter.value : '';
    const selectedLevel = levelFilter ? levelFilter.value : '';
    const sortValue = sortFilter ? sortFilter.value : 'newest';

    // Filter
    filteredCourses = allCourses.filter(course => {
        const matchSearch = course.title.toLowerCase().includes(searchTerm) || 
                            course.instructor.toLowerCase().includes(searchTerm);
        const matchCategory = selectedCategory === '' || course.category === selectedCategory;
        const matchLevel = selectedLevel === '' || course.level === selectedLevel;
        
        return matchSearch && matchCategory && matchLevel;
    });

    // Sort
    filteredCourses.sort((a, b) => {
        switch (sortValue) {
            case 'price-asc':
                return a.price - b.price;
            case 'price-desc':
                return b.price - a.price;
            case 'popular':
                return b.popularity - a.popularity;
            case 'newest':
            default:
                return b.dateAdded - a.dateAdded;
        }
    });

    renderCourses();
    renderPagination();
}

/**
 * Render Courses to Grid
 */
function renderCourses() {
    if (!courseGrid) return;

    courseGrid.innerHTML = '';

    if (filteredCourses.length === 0) {
        courseGrid.innerHTML = `
            <div class="col-12 text-center py-5">
                <i class="fas fa-search fa-3x text-muted mb-3"></i>
                <h4 class="text-muted">No courses found matching your criteria.</h4>
            </div>
        `;
        return;
    }

    const startIndex = (currentPage - 1) * itemsPerPage;
    const endIndex = startIndex + itemsPerPage;
    const paginatedCourses = filteredCourses.slice(startIndex, endIndex);

    paginatedCourses.forEach(course => {
        const priceHtml = course.listedPrice 
            ? `<span class="listed-price">$${course.listedPrice.toFixed(2)}</span><span class="course-price">$${course.price.toFixed(2)}</span>`
            : `<span class="course-price">$${course.price.toFixed(2)}</span>`;

        const cardHtml = `
            <div class="col-12 col-md-6 col-lg-3">
                <div class="card h-100 shadow-sm course-card border-0">
                    <img src="${course.thumbnail}" class="card-img-top course-thumbnail" alt="${course.title}">
                    <div class="card-body d-flex flex-column">
                        <div class="d-flex justify-content-between align-items-center mb-2">
                            <span class="badge bg-primary bg-opacity-10 text-primary">${course.category}</span>
                            <span class="course-level fw-medium"><i class="fas fa-signal me-1 text-muted"></i>${course.level}</span>
                        </div>
                        <h5 class="card-title fw-bold mb-1 text-truncate" title="${course.title}">${course.title}</h5>
                        <p class="card-text text-muted small mb-3"><i class="fas fa-user-tie me-2"></i>${course.instructor}</p>
                        
                        <div class="mt-auto">
                            <div class="d-flex justify-content-between align-items-center mb-3">
                                <span class="text-muted small"><i class="far fa-clock me-1"></i>${course.duration}</span>
                                <div>${priceHtml}</div>
                            </div>
                            <a href="course-detail.html?id=${course.id}" class="btn btn-outline-primary w-100">View Details</a>
                        </div>
                    </div>
                </div>
            </div>
        `;
        courseGrid.insertAdjacentHTML('beforeend', cardHtml);
    });
}

/**
 * Render Pagination
 */
function renderPagination() {
    if (!pagination) return;

    const totalPages = Math.ceil(filteredCourses.length / itemsPerPage);
    pagination.innerHTML = '';

    if (totalPages <= 1) return;

    // Previous Button
    const prevDisabled = currentPage === 1 ? 'disabled' : '';
    pagination.insertAdjacentHTML('beforeend', `
        <li class="page-item ${prevDisabled}">
            <a class="page-link" href="#courses" onclick="changePage(${currentPage - 1}, event)" aria-label="Previous">
                <span aria-hidden="true">&laquo;</span>
            </a>
        </li>
    `);

    // Page Numbers
    for (let i = 1; i <= totalPages; i++) {
        const activeClass = currentPage === i ? 'active' : '';
        pagination.insertAdjacentHTML('beforeend', `
            <li class="page-item ${activeClass}">
                <a class="page-link" href="#courses" onclick="changePage(${i}, event)">${i}</a>
            </li>
        `);
    }

    // Next Button
    const nextDisabled = currentPage === totalPages ? 'disabled' : '';
    pagination.insertAdjacentHTML('beforeend', `
        <li class="page-item ${nextDisabled}">
            <a class="page-link" href="#courses" onclick="changePage(${currentPage + 1}, event)" aria-label="Next">
                <span aria-hidden="true">&raquo;</span>
            </a>
        </li>
    `);
}

/**
 * Change Page
 */
window.changePage = function(page, event) {
    if (event) event.preventDefault();
    
    const totalPages = Math.ceil(filteredCourses.length / itemsPerPage);
    if (page < 1 || page > totalPages) return;
    
    currentPage = page;
    renderCourses();
    renderPagination();
    
    // Scroll to top of courses section
    const coursesSection = document.getElementById('courses');
    if (coursesSection) {
        coursesSection.scrollIntoView({ behavior: 'smooth' });
    }
};

/**
 * Setup Counter Animation using IntersectionObserver
 */
function setupCounterAnimation() {
    const counters = document.querySelectorAll('.counter');
    if (counters.length === 0) return;

    const speed = 200; // The lower the slower

    const animateCounters = (entries, observer) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const counter = entry.target;
                const target = +counter.getAttribute('data-target');
                
                const updateCount = () => {
                    const count = +counter.innerText.replace(/,/g, '');
                    const inc = target / speed;

                    if (count < target) {
                        counter.innerText = Math.ceil(count + inc).toLocaleString();
                        setTimeout(updateCount, 10);
                    } else {
                        counter.innerText = target.toLocaleString();
                    }
                };

                updateCount();
                observer.unobserve(counter); // Only animate once
            }
        });
    };

    const observer = new IntersectionObserver(animateCounters, {
        threshold: 0.5
    });

    counters.forEach(counter => {
        observer.observe(counter);
    });
}
